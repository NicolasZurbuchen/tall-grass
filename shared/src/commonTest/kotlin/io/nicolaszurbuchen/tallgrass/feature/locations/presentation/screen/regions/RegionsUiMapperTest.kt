package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.mapper.toUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegionsUiMapperTest {
    @Test
    fun loading_carriesNoRegionsAndNoError() {
        val model = RegionsState(isLoading = true).toUiModel()

        assertTrue(model.isLoading)
        assertTrue(model.regions.isEmpty())
        assertNull(model.error)
    }

    @Test
    fun loaded_mapsEveryRegionToACard() {
        val state = RegionsState(isLoading = false, regions = listOf(LocationFixtures.kanto, LocationFixtures.orre))

        assertEquals(listOf("kanto", "orre"), state.toUiModel().regions.map { it.slug })
    }

    @Test
    fun cards_canBePassedInRatherThanMapped() {
        // RegionsViewModel holds its own cache and passes it, because mapping the cards is the
        // expensive half of this function.
        val state = RegionsState(isLoading = false, regions = listOf(LocationFixtures.kanto))
        val prebuilt = state.regions.map { it.toUiModel() }

        assertEquals(prebuilt, state.toUiModel(prebuilt).regions)
    }

    @Test
    fun error_reachesTheUiModel() {
        val model = RegionsState(isLoading = false, error = AppError.Unexpected(IllegalStateException())).toUiModel()

        assertNotNull(model.error)
    }
}
