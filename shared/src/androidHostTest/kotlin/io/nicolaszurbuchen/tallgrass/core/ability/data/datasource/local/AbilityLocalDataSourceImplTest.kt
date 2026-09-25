package io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Against a real SQLite file, for the same reason the move and dex sources are: the thing worth
 * pinning is that the query, the generated row type and the mapper agree.
 *
 * Three of these could not be written any other way. The list's order, the holder grid's order and
 * the join that finds a variant's dex card are all facts about the database rather than about the
 * mapper.
 */
class AbilityLocalDataSourceImplTest {
    @Test
    fun abilities_comeBackAlphabeticallyRatherThanInInsertOrder() =
        runTest {
            // An ability has no number to walk, so the ORDER BY is the only thing giving the list a
            // shape. Inserted deliberately out of order.
            val database = inMemoryPokedex()
            database.insertAbility(slug = "levitate", name = "Levitate")
            database.insertAbility(slug = "adaptability", name = "Adaptability")
            database.insertAbility(slug = "stench", name = "Stench")

            val source = source(database)

            assertEquals(listOf("Adaptability", "Levitate", "Stench"), source.abilities().map { it.name })
        }

    @Test
    fun detail_readsBothHalvesOfTheEffectBack() =
        runTest {
            val database = inMemoryPokedex()
            database.insertAbility(
                slug = "levitate",
                name = "Levitate",
                shortEffect = "Evades Ground moves.",
                effect = "Immune to Ground-type moves, Spikes, Toxic Spikes and Arena Trap.",
            )

            val ability = source(database).detail("levitate")

            assertEquals("Evades Ground moves.", ability?.shortEffect)
            assertEquals("Immune to Ground-type moves, Spikes, Toxic Spikes and Arena Trap.", ability?.effect)
        }

    @Test
    fun detail_isNullForASlugWithNoRow() =
        runTest {
            val database = inMemoryPokedex()
            database.insertAbility(slug = "levitate", name = "Levitate")

            assertNull(source(database).detail("missingno"))
        }

    @Test
    fun holders_comeBackInDexOrderRatherThanInsertOrder() =
        runTest {
            // The grid reads as a walk through the Pokedex, which is the same walk the dex itself
            // takes. Inserted backwards.
            val database = inMemoryPokedex()
            database.insertAbility(slug = "levitate", name = "Levitate")
            database.insertVariant(slug = "rotom", dexNumber = 479, name = "Rotom", types = listOf("electric"))
            database.insertVariant(slug = "gastly", dexNumber = 92, name = "Gastly", types = listOf("ghost", "poison"))
            database.variantQueries.insertVariantAbility("rotom", "levitate", false, 1)
            database.variantQueries.insertVariantAbility("gastly", "levitate", false, 1)

            val holders = source(database).holders("levitate")

            assertEquals(listOf("Gastly", "Rotom"), holders.map { it.name })
            assertEquals(PokemonType.GHOST, holders.first().primaryType)
            assertEquals(PokemonType.POISON, holders.first().secondaryType)
        }

    @Test
    fun holders_carryTheHiddenFlagPerPokemonRatherThanPerAbility() =
        runTest {
            // The same ability is a normal slot on one Pokemon and the hidden one on another, which
            // is why the flag is on the join row and the detail counts it.
            val database = inMemoryPokedex()
            database.insertAbility(slug = "levitate", name = "Levitate")
            database.insertVariant(slug = "gastly", dexNumber = 92, name = "Gastly", types = listOf("ghost"))
            database.insertVariant(slug = "vibrava", dexNumber = 329, name = "Vibrava", types = listOf("ground"))
            database.variantQueries.insertVariantAbility("gastly", "levitate", false, 1)
            database.variantQueries.insertVariantAbility("vibrava", "levitate", true, 3)

            val holders = source(database).holders("levitate")

            assertEquals(listOf(false, true), holders.map { it.isHidden })
        }

    @Test
    fun holders_reachAVariantThroughItsSpeciesDexCardRatherThanThroughItself() =
        runTest {
            // Only default forms are cards, so Alolan Sandshrew opens Sandshrew's card with the
            // Alolan form selected. Getting this wrong is what sent every variant to Bulbasaur once.
            val database = inMemoryPokedex()
            database.insertAbility(slug = "slush-rush", name = "Slush Rush")
            database.insertVariant(slug = "sandshrew", dexNumber = 27, name = "Sandshrew", types = listOf("ground"))
            database.insertVariant(
                slug = "sandshrew-alola",
                dexNumber = 27,
                name = "Alolan Sandshrew",
                formLabel = "Alolan",
                listed = false,
                sortOrder = 2,
                types = listOf("ice", "steel"),
            )
            database.variantQueries.insertVariantAbility("sandshrew-alola", "slush-rush", false, 1)

            val holder = source(database).holders("slush-rush").single()

            assertEquals("sandshrew-alola", holder.variantSlug)
            assertEquals("sandshrew", holder.cardSlug)
        }

    @Test
    fun holders_areEmptyForAnAbilityNothingHas() =
        runTest {
            val database = inMemoryPokedex()
            database.insertAbility(slug = "levitate", name = "Levitate")

            assertTrue(source(database).holders("levitate").isEmpty())
        }
}

private fun kotlinx.coroutines.test.TestScope.source(database: PokedexDatabase) =
    AbilityLocalDataSourceImpl(lazyOf(database.abilityQueries), StandardTestDispatcher(testScheduler))

private fun inMemoryPokedex(): PokedexDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PokedexDatabase.Schema.create(driver)
    return PokedexDatabase(driver)
}

private fun PokedexDatabase.insertAbility(
    slug: String,
    name: String,
    shortEffect: String = "Does something.",
    effect: String = "Does something, at greater length.",
) {
    abilityQueries.insertAbility(
        slug = slug,
        name = name,
        generation = 3,
        shortEffect = shortEffect,
        effect = effect,
    )
}

@Suppress("LongParameterList")
private fun PokedexDatabase.insertVariant(
    slug: String,
    dexNumber: Long,
    name: String,
    types: List<String>,
    formLabel: String? = null,
    listed: Boolean = true,
    sortOrder: Long = 1,
) {
    variantQueries.insertVariant(
        slug = slug,
        speciesDexNumber = dexNumber,
        speciesSlug = slug.substringBefore("-"),
        name = name,
        formLabel = formLabel,
        form = formLabel?.let { slug.substringAfter("-", "") }?.ifEmpty { null },
        formKind = if (formLabel == null) "NONE" else "ALTERNATE",
        isMega = false,
        isBattleOnly = false,
        isDefault = formLabel == null,
        listedInDex = listed,
        height = 7,
        weight = 69,
        artworkUrl = "https://example.invalid/$slug.png",
        sortOrder = sortOrder,
    )
    types.forEachIndexed { index, type ->
        variantQueries.insertVariantType(slug, type, (index + 1).toLong())
    }
}
