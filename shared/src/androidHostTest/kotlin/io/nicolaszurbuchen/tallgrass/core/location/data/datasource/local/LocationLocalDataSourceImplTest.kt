package io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.CaptureMethod
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Console
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Against a real SQLite file, for the same reason the ability and dex sources are: what is worth
 * pinning is that the query, the generated row type and the mapper agree.
 *
 * Several of these could not be written any other way. Whether a grey cell survives to be drawn,
 * whether the condition tags come back attached to the right rows, and whether an area keeps its own
 * denominator are all facts about the joins rather than about the mapper.
 */
class LocationLocalDataSourceImplTest {
    @Test
    fun regions_carryTheirBoxArtInSlotOrder() =
        runTest {
            // Two rows per card, read in one query rather than eleven follow-ups, and the slot is
            // what keeps Charizard on the left.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "kanto", name = "Kanto")
            database.insertVariant("charizard", 6)
            database.insertVariant("blastoise", 9)
            database.regionQueries.insertRegionBoxArt("kanto", "blastoise", 2)
            database.regionQueries.insertRegionBoxArt("kanto", "charizard", 1)

            val region = source(database).regions().single()

            assertEquals(listOf("charizard.png", "blastoise.png"), region.boxArt)
        }

    @Test
    fun regions_keepOneWithNoJapaneseName() =
        runTest {
            // Orre, and only Orre. A null there is an answer rather than a broken row, so the card
            // has to survive it.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "orre", name = "Orre", nativeName = null)

            assertNull(source(database).regions().single().nativeName)
        }

    @Test
    fun regionDetail_countsItsPokedexAndIsZeroForOrre() =
        runTest {
            val database = inMemoryPokedex()
            database.insertRegion(slug = "orre", name = "Orre", nativeName = null)

            assertEquals(0, source(database).regionDetail("orre")?.pokedexSize)
        }

    @Test
    fun regionDex_namesTheFormNativeToThatRegion() =
        runTest {
            // #5's correction, at the level it actually has to hold: the same Dex number resolving to
            // a different form depending on which region is asking.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "alola", name = "Alola")
            database.insertVariant("vulpix", 37, types = listOf("fire"))
            database.insertVariant("vulpix-alola", 37, types = listOf("ice"), listed = false)
            database.regionQueries.insertRegionDexEntry("alola", "vulpix-alola", 253, 0)

            val entry = source(database).regionDex("alola").single()

            assertEquals("vulpix-alola", entry.slug)
            assertEquals(37, entry.dexNumber)
        }

    @Test
    fun regionDex_comesBackInItsOwnOrderRatherThanByNumber() =
        runTest {
            // Kalos ships three dexes that each number from 1, so the order is a column. Inserted
            // with the numbers restarting, which is exactly the seam that breaks a sort on number.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "kalos", name = "Kalos")
            database.insertVariant("chespin", 650)
            database.insertVariant("froakie", 656)
            database.regionQueries.insertRegionDexEntry("kalos", "froakie", 1, 1)
            database.regionQueries.insertRegionDexEntry("kalos", "chespin", 1, 0)

            assertEquals(listOf("chespin", "froakie"), source(database).regionDex("kalos").map { it.slug })
        }

    @Test
    fun locationDetail_drawsEveryGameOfTheRegionAndNotOnlyTheGreenOnes() =
        runTest {
            // A grey cell has to be there to be tapped, which is how a reader confirms a negative.
            // See #9. Red is a Kanto game with nothing on this route.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "kanto", name = "Kanto")
            database.insertVersion("red", "Red", "R", Console.GB_GBC.name, 1, order = 1)
            database.insertVersion("heartgold", "HeartGold", "HG", Console.DS.name, 4, order = 0)
            database.regionQueries.insertRegionVersion("kanto", "red")
            database.regionQueries.insertRegionVersion("kanto", "heartgold")
            database.insertLocation("kanto-route-1", "Route 1", "kanto")
            database.insertVariant("pidgey", 16)
            database.insertEncounter(1, "kanto-route-1", "heartgold", "pidgey")

            val route = source(database).locationDetail("kanto-route-1")

            assertEquals(listOf("heartgold", "red"), route?.versions?.map { it.slug })
            assertEquals(setOf("heartgold"), route?.encounteredVersions)
        }

    @Test
    fun locationDetail_isNullForASlugWithNoRow() =
        runTest {
            val database = inMemoryPokedex()
            database.insertRegion(slug = "kanto", name = "Kanto")

            assertNull(source(database).locationDetail("kanto-route-99"))
        }

    @Test
    fun encountersAt_attachEachRowsOwnConditionTags() =
        runTest {
            // Read in one query keyed by encounter id. Getting this join wrong is not a crash: it is
            // a table whose rows carry each other's conditions, and every rate then reads as wrong.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "kanto", name = "Kanto")
            database.insertLocation("kanto-route-1", "Route 1", "kanto")
            database.insertVariant("pidgey", 16)
            database.insertVariant("rattata", 19)
            database.insertEncounter(1, "kanto-route-1", "heartgold", "pidgey", chance = 45)
            database.insertEncounter(2, "kanto-route-1", "heartgold", "rattata", chance = 30)
            database.encounterQueries.insertEncounterCondition(1, "time-day")
            database.encounterQueries.insertEncounterCondition(2, "time-night")

            val rows = source(database).encountersAt("kanto-route-1", "heartgold").associateBy { it.variantSlug }

            assertEquals(listOf("time-day"), rows.getValue("pidgey").conditions)
            assertEquals(listOf("time-night"), rows.getValue("rattata").conditions)
        }

    @Test
    fun encountersAt_readOnlyTheGameAskedFor() =
        runTest {
            // Route 1 means a different table in every game it appears in.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "kanto", name = "Kanto")
            database.insertLocation("kanto-route-1", "Route 1", "kanto")
            database.insertVariant("pidgey", 16)
            database.insertEncounter(1, "kanto-route-1", "heartgold", "pidgey")
            database.insertEncounter(2, "kanto-route-1", "red", "pidgey")

            assertEquals(1, source(database).encountersAt("kanto-route-1", "red").size)
        }

    @Test
    fun encountersAt_keepTheAreaEachRowBelongsTo() =
        runTest {
            // One place can draw the same method from several areas, and an area is the unit a rate
            // is a percentage of. Losing it here is how a bar reaches 200%.
            val database = inMemoryPokedex()
            database.insertRegion(slug = "sinnoh", name = "Sinnoh")
            database.insertLocation("canalave-city", "Canalave City", "sinnoh")
            database.locationQueries.insertLocationArea("canalave-city-sea", "canalave-city", "Sea")
            database.insertVariant("magikarp", 129)
            database.insertEncounter(1, "canalave-city", "diamond", "magikarp", area = "canalave-city")
            database.insertEncounter(2, "canalave-city", "diamond", "magikarp", area = "canalave-city-sea")

            val areas = source(database).encountersAt("canalave-city", "diamond").map { it.areaSlug to it.areaName }

            assertEquals(listOf("canalave-city" to null, "canalave-city-sea" to "Sea"), areas.sortedBy { it.first })
        }

    @Test
    fun encountersFor_readTheSameRowsFromThePokemonSide() =
        runTest {
            val database = inMemoryPokedex()
            database.insertRegion(slug = "kanto", name = "Kanto")
            database.insertLocation("kanto-route-1", "Route 1", "kanto")
            database.insertVariant("pidgey", 16)
            database.insertEncounter(1, "kanto-route-1", "heartgold", "pidgey")

            val row = source(database).encountersFor("pidgey", "heartgold").single()

            assertEquals("Route 1", row.locationName)
        }

    @Test
    fun availability_readsTheNewestGenerationWithDataRatherThanAssumingIt() =
        runTest {
            // Written down as 8 this would be wrong on the day upstream fills Generation IX in. Here
            // the newest generation with any row at all is 4, so a Pokemon found in 4 is not a
            // transfer.
            val database = inMemoryPokedex()
            database.insertVersion("heartgold", "HeartGold", "HG", Console.DS.name, 4, order = 0)
            database.insertRegion(slug = "kanto", name = "Kanto")
            database.insertLocation("kanto-route-1", "Route 1", "kanto")
            database.insertVariant("pidgey", 16)
            database.insertEncounter(1, "kanto-route-1", "heartgold", "pidgey")

            val found = source(database).availabilityFor("pidgey")

            assertEquals(listOf(CaptureMethod.WILD_CATCH), found.captureMethods)
            assertTrue("heartgold" in found.encounteredVersions)
        }

    @Test
    fun availability_callsItTransferOnlyWhenTheNewestGenerationHasNothing() =
        runTest {
            val database = inMemoryPokedex()
            database.insertVersion("red", "Red", "R", Console.GB_GBC.name, 1, order = 1)
            database.insertVersion("sword", "Sword", "Sw", Console.SWITCH.name, 8, order = 0)
            database.insertRegion(slug = "kanto", name = "Kanto")
            database.insertLocation("kanto-route-1", "Route 1", "kanto")
            database.insertVariant("pidgey", 16)
            database.insertVariant("zacian", 888)
            database.insertEncounter(1, "kanto-route-1", "red", "pidgey")
            database.insertEncounter(2, "kanto-route-1", "sword", "zacian")

            val found = source(database).availabilityFor("pidgey")

            assertTrue(CaptureMethod.TRANSFER_ONLY in found.captureMethods)
        }
}

private fun TestScope.source(database: PokedexDatabase) =
    LocationLocalDataSourceImpl(
        lazy { database.regionQueries },
        lazy { database.locationQueries },
        lazy { database.encounterQueries },
        StandardTestDispatcher(testScheduler),
    )

private fun inMemoryPokedex(): PokedexDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PokedexDatabase.Schema.create(driver)
    return PokedexDatabase(driver)
}

private fun PokedexDatabase.insertRegion(
    slug: String,
    name: String,
    nativeName: String? = "カントー",
) {
    regionQueries.insertRegion(
        slug = slug,
        name = name,
        nativeName = nativeName,
        generation = 1,
        blurb = "A region.",
        locationCount = 96,
        sortOrder = 0,
    )
}

private fun PokedexDatabase.insertVersion(
    slug: String,
    name: String,
    code: String,
    console: String,
    generation: Long,
    order: Long,
) {
    regionQueries.insertGameVersion(
        slug = slug,
        name = name,
        code = code,
        console = console,
        generation = generation,
        sortOrder = order,
    )
}

private fun PokedexDatabase.insertLocation(
    slug: String,
    name: String,
    regionSlug: String,
) {
    locationQueries.insertLocation(
        slug = slug,
        name = name,
        regionSlug = regionSlug,
        category = "ROUTE",
        versionCount = 1,
    )
}

private fun PokedexDatabase.insertVariant(
    slug: String,
    dexNumber: Long,
    types: List<String> = listOf("normal"),
    listed: Boolean = true,
) {
    variantQueries.insertVariant(
        slug = slug,
        speciesDexNumber = dexNumber,
        speciesSlug = slug.substringBefore("-"),
        name = slug.replaceFirstChar { it.uppercase() },
        formLabel = null,
        form = null,
        formKind = "NONE",
        isMega = false,
        isBattleOnly = false,
        isDefault = listed,
        listedInDex = listed,
        height = 7,
        weight = 69,
        baseExperience = 64,
        artworkUrl = "$slug.png",
        sortOrder = dexNumber,
    )
    types.forEachIndexed { index, type ->
        variantQueries.insertVariantType(slug, type, (index + 1).toLong())
    }
}

@Suppress("LongParameterList")
private fun PokedexDatabase.insertEncounter(
    id: Long,
    locationSlug: String,
    versionSlug: String,
    variantSlug: String,
    area: String = locationSlug,
    method: String = "walk",
    chance: Long = 45,
) {
    encounterQueries.insertEncounter(
        id = id,
        locationSlug = locationSlug,
        areaSlug = area,
        versionSlug = versionSlug,
        method = method,
        variantSlug = variantSlug,
        minLevel = 2,
        maxLevel = 4,
        chance = chance,
    )
}
