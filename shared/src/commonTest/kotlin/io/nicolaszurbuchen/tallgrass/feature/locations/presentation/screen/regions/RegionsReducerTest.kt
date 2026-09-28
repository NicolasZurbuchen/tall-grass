package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RegionsReducerTest {
    private val reduce = RegionsStoreFactory.ReducerImpl

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            // Otherwise a retry would show the skeleton and the old error banner at once.
            val state =
                RegionsState(error = AppError.Unexpected(IllegalStateException()))
                    .reduce(RegionsMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun regionsLoaded_stopsLoadingAndHoldsTheRegions() =
        with(reduce) {
            val regions = listOf(LocationFixtures.kanto, LocationFixtures.orre)

            val state = RegionsState(isLoading = true).reduce(RegionsMessage.RegionsLoaded(regions))

            assertEquals(regions, state.regions)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun regionsLoaded_handsBackTheSameListInstanceItWasGiven() =
        with(reduce) {
            // Identity is what RegionsViewModel keys its card cache on, so a reducer that copied the
            // list would silently remap every card on every state.
            val regions = listOf(LocationFixtures.kanto)

            val state = RegionsState().reduce(RegionsMessage.RegionsLoaded(regions))

            assertSame(regions, state.regions)
        }

    @Test
    fun loadFailed_stopsLoadingAndKeepsTheError() =
        with(reduce) {
            val state = RegionsState(isLoading = true).reduce(RegionsMessage.LoadFailed(AppError.Database.NotFound))

            assertEquals(false, state.isLoading)
            assertEquals(AppError.Database.NotFound, state.error)
        }
}
