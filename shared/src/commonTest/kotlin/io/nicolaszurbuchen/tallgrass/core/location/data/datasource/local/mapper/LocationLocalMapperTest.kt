package io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.EncounterConditionValue
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectEncountersAtLocation
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectGameVersions
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectLocationsByRegion
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectRegionDex
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.CaptureMethod
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Console
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationCategory
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationLocalMapperTest {
    @Test
    fun gameVersion_carriesTheConsoleItsRowIsDrawnIn() {
        val row = versionRow(console = "DS")

        assertEquals(Console.DS, row.toDomain()?.console)
    }

    @Test
    fun gameVersion_isDroppedWhenTheConsoleIsUnknown() {
        // The grid's rows *are* the consoles, so a version with nowhere to sit has no cell to be.
        // Dropping it is better than drawing it into a row that does not exist.
        assertNull(versionRow(console = "VIRTUAL_BOY").toDomain())
    }

    @Test
    fun location_fallsBackToOtherRatherThanBeingDropped() {
        // The category was never a fact -- it is read out of the slug and about one place in six
        // lands here -- so an unknown name is not a reason to lose the row.
        val row =
            SelectLocationsByRegion(
                slug = "spear-pillar",
                name = "Spear Pillar",
                category = "ZIGGURAT",
                versionCount = 3,
            )

        assertEquals(LocationCategory.OTHER, row.toDomain().category)
        assertEquals("Spear Pillar", row.toDomain().name)
    }

    @Test
    fun regionDex_dropsARowWhoseTypeThisBuildCannotColour() {
        // A dex card with no primary type has nothing to draw its hero from.
        assertNull(dexRow(primaryType = null).toDomain())
        assertNull(dexRow(primaryType = "stellar").toDomain())
        assertEquals(PokemonType.FIRE, dexRow(primaryType = "fire").toDomain()?.primaryType)
    }

    @Test
    fun regionDex_carriesNoFormLabel() {
        // A regional dex names one form per species and never two, so there is no pill to draw: the
        // label exists to tell two cards sharing a Dex number apart, and here there is only one.
        assertNull(dexRow(primaryType = "fire").toDomain()?.formLabel)
    }

    @Test
    fun encounter_isDroppedWithoutADexCardToOpen() {
        // Only default forms are cards, so a row that resolves to none cannot be tapped through.
        assertNull(encounterRow(cardSlug = null).toDomain(emptyList()))
        assertNull(encounterRow(primaryType = null).toDomain(emptyList()))
    }

    @Test
    fun encounter_carriesTheConditionsItWasHandedRatherThanReadingThemBack() {
        // The tags arrive from a second query keyed by encounter id, because a busy route is 42 rows
        // and reading each one's tags after it would be 42 more round trips.
        val mapped = encounterRow().toDomain(listOf("time-night", "swarm-no"))

        assertEquals(listOf("time-night", "swarm-no"), mapped?.conditions)
    }

    @Test
    fun conditionValue_carriesItsAxisAndWhetherItIsTheOrdinaryState() {
        val row = EncounterConditionValue(slug = "time-day", axis = "time", isDefault = true)

        assertEquals("time", row.toDomain().axis)
        assertTrue(row.toDomain().isDefault)
    }

    @Test
    fun capturePills_readAGiftAsAGiftRatherThanAsAWildCatch() {
        // A starter is not findable in the grass, and `gift` is an encounter method upstream, so
        // reading any row at all as a wild catch would say it is.
        val pills = setOf("gift").toCaptureMethodsDomain(generationsPresent = setOf(8), newestGenerationWithData = 8)

        assertEquals(listOf(CaptureMethod.GIFT_OR_EVENT), pills)
    }

    @Test
    fun capturePills_readATradeAsATrade() {
        val pills = setOf("npc-trade").toCaptureMethodsDomain(generationsPresent = setOf(8), newestGenerationWithData = 8)

        assertEquals(listOf(CaptureMethod.TRADE), pills)
    }

    @Test
    fun capturePills_countAnyOtherMethodAsAWildCatch() {
        val pills =
            setOf("walk", "gift").toCaptureMethodsDomain(generationsPresent = setOf(8), newestGenerationWithData = 8)

        assertEquals(listOf(CaptureMethod.WILD_CATCH, CaptureMethod.GIFT_OR_EVENT), pills)
    }

    @Test
    fun capturePills_callItTransferOnlyWhenTheNewestGenerationHasNoRow() {
        // An absence rather than a row, which is the whole of what the pill means. The Pokemon is
        // findable in Generation IV and in nothing since.
        val pills = setOf("walk").toCaptureMethodsDomain(generationsPresent = setOf(4), newestGenerationWithData = 8)

        assertEquals(listOf(CaptureMethod.WILD_CATCH, CaptureMethod.TRANSFER_ONLY), pills)
    }

    @Test
    fun capturePills_areEmptyForAPokemonNothingHasEverMet() {
        // No methods and no generations. Every pill is false, including transfer-only, which claims
        // the Pokemon exists somewhere older -- and nothing here says it does.
        val pills = emptySet<String>().toCaptureMethodsDomain(generationsPresent = emptySet(), newestGenerationWithData = 0)

        assertEquals(emptyList(), pills)
    }

    private fun versionRow(console: String) =
        SelectGameVersions(
            slug = "heartgold",
            name = "HeartGold",
            code = "HG",
            console = console,
            generation = 4,
        )

    private fun dexRow(primaryType: String?) =
        SelectRegionDex(
            number = 37,
            slug = "vulpix",
            speciesDexNumber = 37,
            name = "Vulpix",
            artworkUrl = "vulpix.png",
            primaryType = primaryType,
            secondaryType = null,
        )

    private fun encounterRow(
        cardSlug: String? = "pidgey",
        primaryType: String? = "normal",
    ) = SelectEncountersAtLocation(
        id = 1,
        areaSlug = "kanto-route-1",
        areaName = null,
        method = "walk",
        minLevel = 2,
        maxLevel = 4,
        chance = 45,
        variantSlug = "pidgey",
        cardSlug = cardSlug,
        speciesDexNumber = 16,
        name = "Pidgey",
        artworkUrl = "pidgey.png",
        primaryType = primaryType,
        secondaryType = "flying",
    )
}
