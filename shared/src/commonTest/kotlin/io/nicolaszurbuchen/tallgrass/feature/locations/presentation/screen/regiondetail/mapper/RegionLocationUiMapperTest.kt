package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.LocationCategoryUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegionLocationUiMapperTest {
    @Test
    fun row_carriesTheMarkerForItsKindOfPlace() {
        assertEquals(LocationCategoryUiModel.ROUTE, LocationFixtures.route1Summary.toUiModel().category)
        assertEquals(LocationCategoryUiModel.FOREST, LocationFixtures.berryForestSummary.toUiModel().category)
    }

    @Test
    fun aPlaceWithEncounters_saysHowManyGamesHaveThem() {
        assertTrue(LocationFixtures.route1Summary.toUiModel().hasEncounters)
    }

    @Test
    fun aPlaceWithNone_isNotAZeroCount() {
        // "No encounter data" rather than "0 games": a zero in that position reads as a count that
        // happens to be low rather than as the place having none at all. See #24.
        val empty = LocationFixtures.berryForestSummary.copy(versionCount = 0)

        assertFalse(empty.toUiModel().hasEncounters)
    }

    @Test
    fun oneGame_isNotWrittenAsOneGames() {
        val single = LocationFixtures.berryForestSummary.copy(versionCount = 1)

        assertTrue(single.toUiModel().hasEncounters)
    }
}
