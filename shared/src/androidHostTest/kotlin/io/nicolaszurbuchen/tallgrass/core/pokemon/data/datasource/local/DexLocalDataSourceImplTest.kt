package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Exercises the data source against a real SQLite file rather than a mocked query object.
 *
 * The thing worth testing here is the boundary itself -- that the query, the generated types and the
 * mapper agree -- and a fake `VariantQueries` would assert only that the code calls the method it
 * obviously calls. The in-memory database is the cheapest way to make the real join run.
 */
class DexLocalDataSourceImplTest {
    @Test
    fun dexEntries_returnsOnlyListedVariants_inDexOrder() =
        runTest {
            val database = inMemoryPokedex()
            database.insertVariant("bulbasaur", 1, "Bulbasaur", null, listed = true, sortOrder = 1, types = listOf("grass", "poison"))
            database.insertVariant(
                "charizard-mega-x",
                6,
                "Mega Charizard X",
                "Mega Charizard X",
                listed = false,
                sortOrder = 10034,
                types = listOf("fire"),
            )
            database.insertVariant("vulpix", 37, "Vulpix", null, listed = true, sortOrder = 37, types = listOf("fire"))
            database.insertVariant(
                "vulpix-alola",
                37,
                "Alolan Vulpix",
                "Alolan Form",
                listed = true,
                sortOrder = 10103,
                types = listOf("ice"),
            )

            val source = DexLocalDataSourceImpl(database.variantQueries, StandardTestDispatcher(testScheduler))
            val entries = source.dexEntries()

            assertEquals(listOf("bulbasaur", "vulpix", "vulpix-alola"), entries.map { it.slug })
            assertEquals(listOf(1, 37, 37), entries.map { it.dexNumber })
        }

    @Test
    fun dexEntries_readsBothTypesAndLeavesTheSecondNullWhenThereIsOne() =
        runTest {
            // The regression this pins: the query started as correlated subqueries, which SQLDelight
            // types as non-null, so a single-type Pokemon threw rather than returning null.
            val database = inMemoryPokedex()
            database.insertVariant("bulbasaur", 1, "Bulbasaur", null, listed = true, sortOrder = 1, types = listOf("grass", "poison"))
            database.insertVariant("charmander", 4, "Charmander", null, listed = true, sortOrder = 4, types = listOf("fire"))

            val source = DexLocalDataSourceImpl(database.variantQueries, StandardTestDispatcher(testScheduler))
            val entries = source.dexEntries().associateBy { it.slug }

            assertEquals(PokemonType.GRASS, entries.getValue("bulbasaur").primaryType)
            assertEquals(PokemonType.POISON, entries.getValue("bulbasaur").secondaryType)
            assertEquals(PokemonType.FIRE, entries.getValue("charmander").primaryType)
            assertNull(entries.getValue("charmander").secondaryType)
        }

    @Test
    fun dexEntries_isEmptyWhenNothingIsListed() =
        runTest {
            val database = inMemoryPokedex()
            database.insertVariant(
                "charizard-mega-x",
                6,
                "Mega Charizard X",
                "Mega Charizard X",
                listed = false,
                sortOrder = 10034,
                types = listOf("fire"),
            )

            val source = DexLocalDataSourceImpl(database.variantQueries, StandardTestDispatcher(testScheduler))

            assertEquals(emptyList(), source.dexEntries())
        }
}

private fun inMemoryPokedex(): PokedexDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PokedexDatabase.Schema.create(driver)
    return PokedexDatabase(driver)
}

private fun PokedexDatabase.insertVariant(
    slug: String,
    dexNumber: Long,
    name: String,
    formLabel: String?,
    listed: Boolean,
    sortOrder: Long,
    types: List<String>,
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
