package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Against a real SQLite file: five queries have to agree about one Pokemon here, and a fake set of
 * `Queries` would assert only that the code calls the methods it obviously calls.
 */
class PokemonDetailLocalDataSourceImplTest {
    @Test
    fun detail_readsEveryFormOfTheSpeciesTheSlugBelongsTo() =
        runTest {
            val detail = source(StandardTestDispatcher(testScheduler)).detail("vulpix-alola")

            assertEquals(listOf("vulpix", "vulpix-alola"), detail?.variants?.map { it.slug })
        }

    @Test
    fun detail_answersTheSameWhicheverFormItIsAskedAbout() =
        runTest {
            // A regional form's card and its base form's card are two routes into one screen.
            val source = source(StandardTestDispatcher(testScheduler))

            assertEquals(source.detail("vulpix")?.species, source.detail("vulpix-alola")?.species)
        }

    @Test
    fun detail_readsTheSpeciesLevelFieldsOnce() =
        runTest {
            val detail = source(StandardTestDispatcher(testScheduler)).detail("vulpix-alola")

            assertEquals(37, detail?.species?.dexNumber)
            assertEquals("Vulpix", detail?.species?.name)
            assertEquals(listOf(EggGroup.FIELD), detail?.species?.eggGroups)
        }

    @Test
    fun detail_givesEachFormItsOwnTypesAndStats() =
        runTest {
            // The Species/Variant split earns its keep here: the Alolan form is Ice, not Fire, and
            // reading types off the species would get seventeen of Arceus's eighteen forms wrong.
            val variants = source(StandardTestDispatcher(testScheduler)).detail("vulpix")?.variants?.associateBy { it.slug }

            assertEquals(PokemonType.FIRE, variants?.getValue("vulpix")?.primaryType)
            assertEquals(PokemonType.ICE, variants?.getValue("vulpix-alola")?.primaryType)
            assertEquals(65, variants?.getValue("vulpix")?.stats?.speed)
            assertEquals(58, variants?.getValue("vulpix-alola")?.stats?.speed)
        }

    @Test
    fun detail_isNullForASlugNoVariantCarries() =
        runTest {
            assertNull(source(StandardTestDispatcher(testScheduler)).detail("missingno"))
        }
}

private fun source(dispatcher: CoroutineDispatcher): PokemonDetailLocalDataSourceImpl {
    val database = vulpix()
    return PokemonDetailLocalDataSourceImpl(database.speciesQueries, database.variantQueries, dispatcher)
}

private fun vulpix(): PokedexDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PokedexDatabase.Schema.create(driver)
    val database = PokedexDatabase(driver)

    database.speciesQueries.insertSpecies(
        dexNumber = 37,
        slug = "vulpix",
        name = "Vulpix",
        genus = "Fox Pokémon",
        generation = 1,
        genderRate = 6,
        captureRate = 190,
        hatchCounter = 20,
        growthRate = "medium",
        isBaby = false,
        isLegendary = false,
        isMythical = false,
    )
    database.speciesQueries.insertSpeciesEggGroup(speciesDexNumber = 37, eggGroup = "ground")

    database.insertVariant("vulpix", "Vulpix", null, isDefault = true, sortOrder = 37, type = "fire", speed = 65)
    database.insertVariant("vulpix-alola", "Alolan Vulpix", "Alolan Form", isDefault = false, sortOrder = 10103, type = "ice", speed = 58)

    return database
}

private fun PokedexDatabase.insertVariant(
    slug: String,
    name: String,
    formLabel: String?,
    isDefault: Boolean,
    sortOrder: Long,
    type: String,
    speed: Long,
) {
    variantQueries.insertVariant(
        slug = slug,
        speciesDexNumber = 37,
        speciesSlug = "vulpix",
        name = name,
        formLabel = formLabel,
        form = if (isDefault) null else "alola",
        formKind = if (isDefault) "NONE" else "REGIONAL",
        isMega = false,
        isBattleOnly = false,
        isDefault = isDefault,
        listedInDex = true,
        height = 6,
        weight = 99,
        artworkUrl = "https://example.invalid/$slug.png",
        sortOrder = sortOrder,
    )
    variantQueries.insertVariantType(slug, type, 1)

    listOf("hp" to 38L, "attack" to 41L, "defense" to 40L, "special-attack" to 50L, "special-defense" to 65L, "speed" to speed)
        .forEach { (statSlug, value) -> variantQueries.insertVariantStat(slug, statSlug, value) }
}
