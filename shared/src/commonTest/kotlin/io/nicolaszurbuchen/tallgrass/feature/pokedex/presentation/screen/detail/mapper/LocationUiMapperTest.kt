package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.CaptureMethod
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.CaptureMethodUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.CatchDifficultyUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationUiMapperTest {
    private val availability = LocationFixtures.pidgeyAvailability

    @Test
    fun notReadYet_isDifferentFromNothingFound() {
        // Null means the tab has not been opened. An empty result means the Pokemon is nowhere,
        // which is a real answer, so the two must not look the same.
        val unread = null.toLocationUiModel(45, null, isLoadingPlaces = false, places = emptyList())

        assertTrue(unread.isLoading)
        assertNull(unread.emptyText)
    }

    @Test
    fun theGridSpansEveryGameRatherThanOneRegion() {
        // Unlike the route side's. The question here is which of my games has this, and the answer
        // crosses all of them.
        val model = availability.toLocationUiModel(45, null, isLoadingPlaces = false, places = emptyList())
        val cells = model.grid.rows.flatMap { it.cells }

        assertEquals(setOf("heartgold", "red", "y"), cells.map { it.slug }.toSet())
        assertEquals(setOf("heartgold", "red"), cells.filter { it.isAvailable }.map { it.slug }.toSet())
    }

    @Test
    fun aPokemonFoundNowhere_saysSoBeforeAnyCellIsTapped() {
        // Arceus and the starters -- 17.5% of species. A fact about the Pokemon rather than about a
        // game, so it does not wait for one to be chosen.
        val never =
            VariantAvailability(
                variantSlug = "arceus",
                captureMethods = listOf(CaptureMethod.TRANSFER_ONLY),
                versions = listOf(LocationFixtures.red),
                encounteredVersions = emptySet(),
            )

        assertNotNull(never.toLocationUiModel(3, null, isLoadingPlaces = false, places = emptyList()).emptyText)
    }

    @Test
    fun aChosenGameWithNoRows_saysTransferItIn() {
        // Pokemon Y is in the grid and not in the set. The wording is about that game rather than
        // about the Pokemon, which is the distinction #21 insists on.
        val model = availability.toLocationUiModel(45, "y", isLoadingPlaces = false, places = emptyList())

        assertNotNull(model.emptyText)
        assertEquals("y", model.selected?.slug)
    }

    @Test
    fun whileThePlacesLoad_nothingIsSaidEitherWay() {
        val model = availability.toLocationUiModel(45, "heartgold", isLoadingPlaces = true, places = emptyList())

        assertNull(model.emptyText)
        assertNull(model.breadcrumbText)
    }

    @Test
    fun capturePills_crossToTheirRenderingHalf() {
        val model = availability.toLocationUiModel(45, null, isLoadingPlaces = false, places = emptyList())

        assertEquals(listOf(CaptureMethodUiModel.WILD_CATCH), model.captureMethods)
    }

    @Test
    fun catchRate_carriesBothTheFigureAndAWordForIt() {
        // #43 asks only for the word. The figure ships too, because it is the fact the word reads.
        val model = availability.toLocationUiModel(45, null, isLoadingPlaces = false, places = emptyList())

        assertEquals(CatchDifficultyUiModel.HARD, model.catchDifficulty)
        assertEquals(45f / 255f, model.catchFraction)
    }

    @Test
    fun catchDifficulty_bandsRunFromCaterpieToLegendary() {
        assertEquals(CatchDifficultyUiModel.VERY_EASY, 255.toCatchDifficultyUiModel())
        assertEquals(CatchDifficultyUiModel.EASY, 190.toCatchDifficultyUiModel())
        assertEquals(CatchDifficultyUiModel.MODERATE, 75.toCatchDifficultyUiModel())
        assertEquals(CatchDifficultyUiModel.HARD, 45.toCatchDifficultyUiModel())
        assertEquals(CatchDifficultyUiModel.VERY_HARD, 3.toCatchDifficultyUiModel())
    }

    @Test
    fun places_foldToOneRowPerPlaceAndMethod() {
        // One Pokemon in one game is one to three rows, which is why this side has no method tabs.
        val rows =
            listOf(
                LocationFixtures.pidgeyOnRoute1,
                LocationFixtures.pidgeyOnRoute1.copy(id = 2, chance = 20, conditions = listOf("time-night")),
            ).toVariantPlacesUiModel()

        assertEquals(1, rows.size)
        assertEquals("Route 1", rows.single().locationName)
    }

    @Test
    fun aRateHereIsAlwaysABestCase() {
        // Nothing on this side pins a state, so a conditioned row can never be exact -- and the
        // higher of two states wins rather than the two being added.
        val rows =
            listOf(
                LocationFixtures.pidgeyOnRoute1.copy(chance = 45, conditions = listOf("time-day")),
                LocationFixtures.pidgeyOnRoute1.copy(id = 2, chance = 20, conditions = listOf("time-night")),
            ).toVariantPlacesUiModel()

        assertNotNull(rows.single().rateText)
        assertNotNull(rows.single().conditionText)
    }

    @Test
    fun aMethodWithNoMeaningfulRate_printsNoFigure() {
        val gift: List<VariantEncounter> =
            listOf(LocationFixtures.pidgeyOnRoute1.copy(chance = 0, method = "gift", conditions = emptyList()))

        val row = gift.toVariantPlacesUiModel().single()

        assertNull(row.rateText)
        assertNull(row.conditionText)
    }
}
