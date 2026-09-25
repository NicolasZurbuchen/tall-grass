package io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.BattleStat
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveAilment
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveCategory
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Against a real SQLite file, for the same reason the dex and type sources are: the thing worth
 * pinning is that the query, the generated row type and the mapper agree.
 *
 * Two of these could not be written any other way. The list is sorted by SQL, and the stat changes
 * come back in primary-key order from a table with no ordering column — both are facts about the
 * database rather than about the mapper.
 */
class MoveLocalDataSourceImplTest {
    @Test
    fun moves_comeBackAlphabeticallyRatherThanInInsertOrder() =
        runTest {
            // A move has no number to walk, so the ORDER BY is the only thing giving the list a
            // shape. Inserted deliberately out of order.
            val database = inMemoryPokedex()
            database.insertMove(slug = "thunderbolt", name = "Thunderbolt")
            database.insertMove(slug = "absorb", name = "Absorb")
            database.insertMove(slug = "flamethrower", name = "Flamethrower")

            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))

            assertEquals(listOf("Absorb", "Flamethrower", "Thunderbolt"), source.moves().map { it.name })
        }

    @Test
    fun detail_readsTheMetaColumnsBackAsAMeta() =
        runTest {
            val database = inMemoryPokedex()
            database.insertMove(
                slug = "flamethrower",
                name = "Flamethrower",
                category = "damage-ailment",
                ailment = "burn",
                ailmentChance = 10,
            )

            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))
            val move = source.detail("flamethrower")

            assertEquals(MoveCategory.DAMAGE_AILMENT, move?.meta?.category)
            assertEquals(MoveAilment.BURN, move?.meta?.ailment)
            assertEquals(10, move?.meta?.ailmentChance)
        }

    @Test
    fun detail_hasNoMetaForAMoveWhoseMetaColumnsAreEmpty() =
        runTest {
            // The 92 Generation VIII and IX moves. Every meta column is null together, and the whole
            // object is absent rather than a record of nulls.
            val database = inMemoryPokedex()
            database.insertMove(slug = "dire-claw", name = "Dire Claw")

            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))

            assertNull(source.detail("dire-claw")?.meta)
        }

    @Test
    fun detail_ordersStatChangesTheWayStatsAreDrawnRatherThanHowSqliteReturnsThem() =
        runTest {
            // The table's primary key is (moveSlug, statSlug), so its rows come back alphabetically:
            // attack, special-attack, speed. The order a reader expects is the stat table's, which
            // puts Speed last. This is the test that would have caught shipping SQLite's order.
            val database = inMemoryPokedex()
            database.insertMove(slug = "ancient-power", name = "Ancient Power")
            listOf("speed", "attack", "special-attack").forEach {
                database.moveQueries.insertMoveStatChange("ancient-power", it, 1)
            }

            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))

            assertEquals(
                listOf(BattleStat.ATTACK, BattleStat.SPECIAL_ATTACK, BattleStat.SPEED),
                source.detail("ancient-power")?.statChanges?.map { it.stat },
            )
        }

    @Test
    fun detail_isNullForASlugTheDatabaseDoesNotHold() =
        runTest {
            val database = inMemoryPokedex()
            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))

            assertNull(source.detail("missingno"))
        }

    @Test
    fun moves_dropARowWhoseTypeThisBuildDoesNotKnow() =
        runTest {
            // A disagreement between the bundled dataset and this build. The rest of the list still
            // reads rather than the whole screen failing.
            val database = inMemoryPokedex()
            database.insertMove(slug = "tera-blast", name = "Tera Blast", typeSlug = "stellar")
            database.insertMove(slug = "absorb", name = "Absorb")

            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))

            assertEquals(listOf("Absorb"), source.moves().map { it.name })
            assertTrue(source.moves().size == 1)
        }

    /**
     * **The order the Moves tab reads in, which SQL does not produce.**
     *
     * `selectMovesForVariant` is deliberately unordered: level-up first and by level within it, then
     * the three methods that have no level, is `LearnMethod`'s declaration order, and a CASE
     * expression in the query would be a second copy of it to keep in step. This is the test that
     * would catch shipping whatever order SQLite happened to return.
     */
    @Test
    fun movesFor_readLevelUpFirstAndByLevelWithinIt() =
        runTest {
            val database = inMemoryPokedex()
            database.insertMove(slug = "flamethrower", name = "Flamethrower")
            database.insertMove(slug = "ember", name = "Ember")
            database.insertMove(slug = "earthquake", name = "Earthquake")
            database.insertMove(slug = "dragon-dance", name = "Dragon Dance")

            // Inserted worst-first: a TM, then a late level-up, then an egg move, then an early one.
            database.moveQueries.insertMoveLearner("earthquake", "charizard", "machine", null)
            database.moveQueries.insertMoveLearner("flamethrower", "charizard", "level-up", 46)
            database.moveQueries.insertMoveLearner("dragon-dance", "charizard", "egg", null)
            database.moveQueries.insertMoveLearner("ember", "charizard", "level-up", 4)

            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))

            assertEquals(
                listOf("Ember", "Flamethrower", "Earthquake", "Dragon Dance"),
                source.movesFor("charizard").map { it.name },
            )
        }

    @Test
    fun movesFor_isEmptyForAFormThatLearnsNothingOfItsOwn() =
        runTest {
            // Every Mega and Gigantamax. An empty list is a fact about the form rather than a join
            // that missed.
            val database = inMemoryPokedex()
            database.insertMove(slug = "flamethrower", name = "Flamethrower")
            database.moveQueries.insertMoveLearner("flamethrower", "charizard", "level-up", 46)

            val source = MoveLocalDataSourceImpl(lazyOf(database.moveQueries), StandardTestDispatcher(testScheduler))

            assertTrue(source.movesFor("charizard-mega-x").isEmpty())
        }
}

private fun inMemoryPokedex(): PokedexDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PokedexDatabase.Schema.create(driver)
    return PokedexDatabase(driver)
}

@Suppress("LongParameterList")
private fun PokedexDatabase.insertMove(
    slug: String,
    name: String,
    typeSlug: String = "fire",
    damageClass: String = "special",
    category: String? = null,
    ailment: String? = null,
    ailmentChance: Long? = null,
) {
    moveQueries.insertMove(
        slug = slug,
        name = name,
        generation = 1,
        typeSlug = typeSlug,
        damageClass = damageClass,
        power = 90,
        accuracy = 100,
        pp = 15,
        priority = 0,
        target = "selected-pokemon",
        shortEffect = null,
        effect = null,
        category = category,
        ailment = ailment,
        ailmentChance = ailmentChance,
        minHits = null,
        maxHits = null,
        minTurns = null,
        maxTurns = null,
        drain = null,
        healing = null,
        critRate = null,
        flinchChance = null,
        statChance = null,
    )
}
