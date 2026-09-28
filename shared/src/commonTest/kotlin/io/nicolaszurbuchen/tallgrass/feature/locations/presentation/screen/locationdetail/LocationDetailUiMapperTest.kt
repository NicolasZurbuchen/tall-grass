package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_no_data_yet
import tallgrass.shared.generated.resources.location_detail_not_in_game
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationDetailUiMapperTest {
    private val loaded =
        LocationDetailState(
            isLoading = false,
            location = LocationFixtures.route1,
            conditions = LocationFixtures.timeOfDay,
        )

    @Test
    fun withNoGameChosen_theGridIsOpenAndNothingIsSelected() {
        val model = loaded.toUiModel()

        assertNull(model.selected)
        assertNull(model.breadcrumbText)
        assertTrue(model.areas.isEmpty())
    }

    @Test
    fun theGrid_drawsEveryGameOfTheRegionAndMarksOnlyTheGreenOnes() {
        val cells = loaded.toUiModel().grid.rows.flatMap { it.cells }.associateBy { it.slug }

        assertEquals(setOf("heartgold", "red"), cells.keys)
        assertTrue(cells.getValue("heartgold").isAvailable)
        assertTrue(!cells.getValue("red").isAvailable)
    }

    @Test
    fun choosingAGame_collapsesTheGridIntoABreadcrumb() {
        val model =
            loaded.copy(
                version = "heartgold",
                encounters = listOf(LocationFixtures.pidgeyByDay, LocationFixtures.rattataByNight),
            ).toUiModel()

        assertEquals("heartgold", model.selected?.slug)
        assertNotNull(model.breadcrumbText)
        assertEquals(1, model.areas.size)
    }

    @Test
    fun whileTheRowsLoad_theBreadcrumbSaysNothingRatherThanZero() {
        // Otherwise the line would read "0 species" for the beat between the tap and the read, which
        // is the one wording that is never true.
        val model = loaded.copy(version = "heartgold", isLoadingEncounters = true).toUiModel()

        assertNotNull(model.selected)
        assertNull(model.breadcrumbText)
        assertNull(model.emptyText)
    }

    @Test
    fun aGameWithNothingHere_saysSoRatherThanShowingAGap() {
        // Red is a Kanto game with no rows on this route. That is a fact about the game, not a hole
        // in the dataset, and #21 insists the two are not worded the same.
        val model = loaded.copy(version = "red").toUiModel()

        assertEquals(UiText.Resource(Res.string.location_detail_not_in_game, listOf("Red")), model.emptyText)
    }

    @Test
    fun aGenerationNineGame_saysTheDataIsNotThereYet() {
        // The other empty state. Decided on the generation rather than on a list of games, so it
        // disappears by itself the day upstream fills Generation IX in.
        val scarlet = LocationFixtures.red.copy(slug = "scarlet", name = "Scarlet", code = "Sc", generation = 9)
        val paldea = LocationFixtures.route1.copy(versions = listOf(scarlet), encounteredVersions = emptySet())

        val model = loaded.copy(location = paldea, version = "scarlet").toUiModel()

        assertEquals(UiText.Resource(Res.string.location_detail_no_data_yet), model.emptyText)
    }

    @Test
    fun methods_areListedInTheOrderTheRowsArrive() {
        val model =
            loaded.copy(
                version = "heartgold",
                encounters =
                    listOf(
                        LocationFixtures.pidgeyByDay,
                        LocationFixtures.rattataByNight.copy(method = "headbutt"),
                    ),
            ).toUiModel()

        assertEquals(listOf("walk", "headbutt"), model.methods.map { it.slug })
    }

    @Test
    fun theFirstMethodIsSelectedWhenTheStoreHasNotPickedOne() {
        // A route has no fixed set of methods, so there is nothing to default to until the rows are
        // in -- and once they are, the first one is what the reader sees.
        val model =
            loaded.copy(version = "heartgold", encounters = listOf(LocationFixtures.pidgeyByDay)).toUiModel()

        assertEquals("walk", model.selectedMethod)
    }

    @Test
    fun onlyTheChosenMethodsRowsAreDrawn() {
        val model =
            loaded.copy(
                version = "heartgold",
                method = "walk",
                encounters =
                    listOf(
                        LocationFixtures.pidgeyByDay,
                        LocationFixtures.rattataByNight.copy(method = "headbutt"),
                    ),
            ).toUiModel()

        assertEquals(listOf("pidgey"), model.areas.flatMap { it.rows }.map { it.variantSlug })
    }

    @Test
    fun error_reachesTheUiModel() {
        assertNotNull(loaded.copy(error = AppError.Database.NotFound).toUiModel().error)
    }
}
