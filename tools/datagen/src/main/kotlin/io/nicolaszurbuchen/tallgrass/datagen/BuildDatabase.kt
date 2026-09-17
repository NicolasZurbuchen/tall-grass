package io.nicolaszurbuchen.tallgrass.datagen

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json { ignoreUnknownKeys = false }

/**
 * Builds `pokedex.db` from the committed JSON.
 *
 * Offline by construction: every input is in the repository, so this runs in CI and on a machine
 * with no network. Only [main] in `GenerateDataset` reaches upstream.
 *
 * The database is written through `PokedexDatabase` -- the same generated schema the app queries --
 * rather than through hand-written SQL. A column the app reads and this file forgets to populate
 * cannot exist, because both sides are generated from one set of `.sq` files.
 */
fun main(args: Array<String>) {
    val datasetDir = File(args[0])
    val outputFile = File(args[1])

    val manifest = json.decodeFromString<Manifest>(datasetDir.resolve("manifest.json").readText())
    val types = json.decodeFromString<TypeChartJson>(datasetDir.resolve("types.json").readText())
    val species = json.decodeFromString<List<SpeciesJson>>(datasetDir.resolve("species.json").readText())
    val variants = json.decodeFromString<List<VariantJson>>(datasetDir.resolve("variants.json").readText())

    // Rebuilt from scratch every time. This database is replaced whole-file rather than migrated, so
    // there is nothing in the previous copy worth keeping and an append would silently double rows.
    outputFile.parentFile.mkdirs()
    outputFile.delete()

    val driver = JdbcSqliteDriver("jdbc:sqlite:${outputFile.absolutePath}")
    PokedexDatabase.Schema.create(driver)

    // Stamp the schema version into the file.
    //
    // `Schema.create` builds the tables but leaves SQLite's `user_version` at 0, and that pragma is
    // the only thing a driver looks at to decide whether a database is empty. Ship it at 0 and
    // AndroidSqliteDriver opens a fully populated file, reads "version 0", and runs `create` again
    // -- which fails on the first CREATE TABLE because the table is already there.
    //
    // It cannot fail anywhere except on a device: the generator writes the file, the host tests
    // build their own in memory through the same `create` path, and none of them re-open a
    // pre-populated database the way the app does on first run.
    driver.execute(null, "PRAGMA user_version = ${PokedexDatabase.Schema.version};", 0)

    val database = PokedexDatabase(driver)

    database.transaction {
        types.types.forEach { database.typeQueries.insertType(it.slug, it.name, it.generation.toLong()) }
        types.efficacies.forEach {
            database.typeQueries.insertTypeEfficacy(it.damage, it.target, it.factorPercent.toLong())
        }

        species.forEach { entry ->
            database.speciesQueries.insertSpecies(
                dexNumber = entry.dexNumber.toLong(),
                slug = entry.slug,
                name = entry.name,
                genus = entry.genus,
                generation = entry.generation.toLong(),
                genderRate = entry.genderRate.toLong(),
                captureRate = entry.captureRate.toLong(),
                hatchCounter = entry.hatchCounter.toLong(),
                growthRate = entry.growthRate,
                evolutionChainId = entry.evolutionChainId.toLong(),
            )
            entry.eggGroups.forEach { group ->
                database.speciesQueries.insertSpeciesEggGroup(entry.dexNumber.toLong(), group)
            }
        }

        variants.forEach { entry ->
            database.variantQueries.insertVariant(
                slug = entry.slug,
                speciesDexNumber = entry.speciesDexNumber.toLong(),
                name = entry.name,
                formLabel = entry.formLabel,
                isDefault = if (entry.isDefault) 1L else 0L,
                listedInDex = if (entry.listedInDex) 1L else 0L,
                height = entry.height.toLong(),
                weight = entry.weight.toLong(),
                artworkUrl = entry.artworkUrl,
                sortOrder = entry.sortOrder.toLong(),
            )
            entry.types.forEachIndexed { index, type ->
                database.variantQueries.insertVariantType(entry.slug, type, (index + 1).toLong())
            }
            entry.stats.forEach { (stat, value) ->
                database.variantQueries.insertVariantStat(entry.slug, stat, value.toLong())
            }
            entry.abilities.forEach { ability ->
                database.variantQueries.insertVariantAbility(
                    variantSlug = entry.slug,
                    abilitySlug = ability.slug,
                    abilityName = ability.name,
                    isHidden = if (ability.isHidden) 1L else 0L,
                    slot = ability.slot.toLong(),
                )
            }
        }
    }

    // The manifest's counts are what the generator believed it wrote. Comparing them with what the
    // database actually holds is what turns a truncated read or a dropped row into a failed build
    // instead of a dex with a hole in it.
    val written =
        Manifest(
            schemaVersion = manifest.schemaVersion,
            sourceSha = manifest.sourceSha,
            speciesCount = database.speciesQueries.countSpecies().executeAsOne().toInt(),
            variantCount = database.variantQueries.countVariants().executeAsOne().toInt(),
            listedVariantCount = database.variantQueries.countListedVariants().executeAsOne().toInt(),
            typeCount = database.typeQueries.countTypes().executeAsOne().toInt(),
        )
    check(written == manifest) { "Database disagrees with the manifest.\n  manifest: $manifest\n  database: $written" }

    // Read the stamp back rather than trusting the write. A database that ships with the wrong
    // version does not fail here, in a test, or on any host: it fails on a real device, on first
    // run, as "something went wrong" on the dex screen.
    val stamped =
        driver
            .executeQuery(null, "PRAGMA user_version;", { cursor ->
                cursor.next()
                QueryResult.Value(cursor.getLong(0))
            }, 0)
            .value
    check(stamped == PokedexDatabase.Schema.version) {
        "pokedex.db is stamped user_version=$stamped but the schema is version ${PokedexDatabase.Schema.version}. " +
            "A driver would read that as an empty database and try to create the tables again."
    }

    driver.close()
    println("Wrote ${outputFile.name} (${outputFile.length() / 1024} KiB) from ${manifest.sourceSha.take(7)}")
}
