package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegionDetailReducerTest {
    private val reduce = RegionDetailStoreFactory.ReducerImpl

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            val state =
                RegionDetailState(error = AppError.Database.NotFound)
                    .reduce(RegionDetailMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun regionLoaded_fillsAllThreeTabsAtOnce() =
        with(reduce) {
            // Three reads of one local database is one round trip, and a tab that populates a beat
            // after it is tapped reads as slower than one that was always ready.
            val state =
                RegionDetailState(isLoading = true).reduce(
                    RegionDetailMessage.RegionLoaded(
                        region = LocationFixtures.kantoDetail,
                        locations = listOf(LocationFixtures.route1Summary),
                        dex = emptyList(),
                    ),
                )

            assertEquals(LocationFixtures.kantoDetail, state.region)
            assertEquals(1, state.locations.size)
            assertEquals(false, state.isLoading)
        }

    @Test
    fun tabChanged_movesTheTabAndTouchesNothingElse() =
        with(reduce) {
            val before = RegionDetailState(region = LocationFixtures.kantoDetail, query = "route")
            val after = before.reduce(RegionDetailMessage.TabChanged(RegionDetailState.Tab.POKEDEX))

            assertEquals(RegionDetailState.Tab.POKEDEX, after.tab)
            assertEquals("route", after.query)
            assertEquals(before.region, after.region)
        }

    @Test
    fun queryChanged_survivesATabChange() =
        with(reduce) {
            // The tabs are a pager, so swiping away and back would clear what was typed if the query
            // lived in the composable rather than here.
            val typed = RegionDetailState().reduce(RegionDetailMessage.QueryChanged("berry"))
            val swiped = typed.reduce(RegionDetailMessage.TabChanged(RegionDetailState.Tab.ABOUT))

            assertEquals("berry", swiped.query)
        }

    @Test
    fun loadFailed_stopsLoadingAndKeepsTheError() =
        with(reduce) {
            val state =
                RegionDetailState(isLoading = true)
                    .reduce(RegionDetailMessage.LoadFailed(AppError.Database.NotFound))

            assertEquals(false, state.isLoading)
            assertEquals(AppError.Database.NotFound, state.error)
        }
}
