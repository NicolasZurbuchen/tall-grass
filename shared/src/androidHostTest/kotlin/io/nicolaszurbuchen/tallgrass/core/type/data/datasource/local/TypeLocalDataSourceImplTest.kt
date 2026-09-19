package io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Against a real SQLite file, for the same reason the dex source is: the thing worth pinning is that
 * the query, the generated row type and the mapper agree about the chart.
 */
class TypeLocalDataSourceImplTest {
    @Test
    fun efficaciesAgainst_readsOnlyTheCellsAimedAtThatType() =
        runTest {
            val database = inMemoryPokedex()
            database.insertEfficacy(damage = "rock", target = "fire", percent = 200)
            database.insertEfficacy(damage = "water", target = "fire", percent = 200)
            database.insertEfficacy(damage = "grass", target = "fire", percent = 50)
            database.insertEfficacy(damage = "rock", target = "flying", percent = 200)

            val source = TypeLocalDataSourceImpl(lazyOf(database.typeQueries), StandardTestDispatcher(testScheduler))
            val cells = source.efficaciesAgainst(PokemonType.FIRE).associate { it.damageType to it.factorPercent }

            assertEquals(mapOf(PokemonType.ROCK to 200, PokemonType.WATER to 200, PokemonType.GRASS to 50), cells)
        }

    @Test
    fun efficaciesAgainst_isEmptyForATypeWithNoNonNeutralCells() =
        runTest {
            val database = inMemoryPokedex()
            database.insertEfficacy(damage = "rock", target = "fire", percent = 200)

            val source = TypeLocalDataSourceImpl(lazyOf(database.typeQueries), StandardTestDispatcher(testScheduler))

            assertEquals(emptyList(), source.efficaciesAgainst(PokemonType.NORMAL))
        }
}

private fun inMemoryPokedex(): PokedexDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PokedexDatabase.Schema.create(driver)
    return PokedexDatabase(driver)
}

private fun PokedexDatabase.insertEfficacy(
    damage: String,
    target: String,
    percent: Long,
) {
    typeQueries.insertTypeEfficacy(damageTypeSlug = damage, targetTypeSlug = target, factorPercent = percent)
}
