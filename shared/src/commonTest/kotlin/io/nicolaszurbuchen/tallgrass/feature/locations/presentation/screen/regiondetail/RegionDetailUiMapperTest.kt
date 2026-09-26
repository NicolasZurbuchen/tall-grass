package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegionDetailUiMapperTest {
    private val loaded =
        RegionDetailState(
            isLoading = false,
            region = LocationFixtures.kantoDetail,
            locations = listOf(LocationFixtures.route1Summary, LocationFixtures.berryForestSummary),
        )

    @Test
    fun loading_hasNoNameToDrawYet() {
        // Nothing travels into this screen, so there is no name until the read lands and the header
        // draws an empty string rather than a placeholder.
        val model = RegionDetailState(isLoading = true).toUiModel()

        assertTrue(model.isLoading)
        assertEquals("", model.name)
        assertNull(model.about)
    }

    @Test
    fun about_countsTheDexTheLocationsAndTheGames() {
        val about = loaded.toUiModel().about

        assertEquals(UiText.Raw("151"), about?.pokedexText)
        assertEquals(UiText.Raw("96"), about?.locationsText)
        assertEquals(UiText.Raw("2"), about?.gamesText)
    }

    @Test
    fun about_showsADashRatherThanAZeroForARegionWithNoDex() {
        // Orre. A zero in a row of figures reads as a figure, and this one is upstream having no
        // regional dex at all.
        val about = loaded.copy(region = LocationFixtures.orreDetail).toUiModel().about

        assertEquals(UiText.Raw("—"), about?.pokedexText)
    }

    @Test
    fun about_leavesTheJapaneseNameOutWhenThereIsNone() {
        assertNull(loaded.copy(region = LocationFixtures.orreDetail).toUiModel().about?.nativeName)
        assertEquals("カントー", loaded.toUiModel().about?.nativeName)
    }

    @Test
    fun about_listsTheGamesByName() {
        assertEquals("HeartGold · Red", loaded.toUiModel().about?.gamesList)
    }

    @Test
    fun anEmptyQuery_showsEveryPlaceAndNoCount() {
        // The unfiltered count is already on the About tab and on the card that opened the screen,
        // so a third copy under the list would be furniture.
        val model = loaded.toUiModel()

        assertEquals(2, model.locations.size)
        assertNull(model.matchesText)
    }

    @Test
    fun aQuery_filtersByNameAndSaysHowMuchIsLeft() {
        val model = loaded.copy(query = "berry").toUiModel()

        assertEquals(listOf("berry-forest"), model.locations.map { it.slug })
        assertNotNull(model.matchesText)
    }

    @Test
    fun aQuery_readsHyphensAsSpaces() {
        // The names are written with spaces and the slugs with hyphens, and a reader types what
        // they see. "route 1" has to find Route 1.
        assertEquals(listOf("kanto-route-1"), loaded.copy(query = "route 1").toUiModel().locations.map { it.slug })
    }

    @Test
    fun aQuery_ignoresCaseAndSurroundingSpace() {
        assertEquals(1, loaded.copy(query = "  BERRY ").toUiModel().locations.size)
    }

    @Test
    fun aQueryThatMatchesNothing_isEmptyRatherThanUnfiltered() {
        val model = loaded.copy(query = "zzz").toUiModel()

        assertTrue(model.locations.isEmpty())
        assertNotNull(model.matchesText)
    }

    @Test
    fun tab_crossesToItsRenderingHalf() {
        assertEquals(RegionTabUiModel.ABOUT, loaded.toUiModel().tab)
        assertEquals(
            RegionTabUiModel.POKEDEX,
            loaded.copy(tab = RegionDetailState.Tab.POKEDEX).toUiModel().tab,
        )
    }

    @Test
    fun error_reachesTheUiModel() {
        assertNotNull(loaded.copy(error = AppError.Database.NotFound).toUiModel().error)
    }

    @Test
    fun anEmptyDex_isNotAnError() {
        // Orre reaches the Pokedex tab with nothing in it, and that is an answer rather than a
        // failure: the tab says so in its own words instead of borrowing the error banner.
        val model = loaded.copy(region = LocationFixtures.orreDetail).toUiModel()

        assertTrue(model.dex.isEmpty())
        assertNull(model.error)
    }
}
