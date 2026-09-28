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
    fun about_carriesTheNativeNameAndTheDexSize() {
        assertEquals("カントー", loaded.toUiModel().about?.nativeName)
        assertEquals(UiText.Raw("151"), loaded.toUiModel().about?.pokedexText)
    }

    @Test
    fun about_listsTheGamesByName() {
        assertEquals("HeartGold · Red", loaded.toUiModel().about?.gamesList)
    }

    @Test
    fun locations_areEveryPlaceInTheRegion() {
        // There is no filtering left to test: the search field lost focus on every keystroke inside
        // the pager and was removed rather than left broken.
        // In the order the repository gave them, which is by name. The mapper does not re-sort.
        assertEquals(listOf("kanto-route-1", "berry-forest"), loaded.toUiModel().locations.map { it.slug })
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
        // Johto reaches the Pokedex tab with nothing in it, and that is an answer rather than a
        // failure: the tab says so in its own words instead of borrowing the error banner.
        val model = loaded.copy(region = LocationFixtures.johtoDetail).toUiModel()

        assertTrue(model.dex.isEmpty())
        assertNull(model.error)
    }
}
