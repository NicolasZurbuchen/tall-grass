package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetRegionLocationsUseCaseTest {
    private val locations =
        mapOf("kanto" to listOf(LocationFixtures.berryForestSummary, LocationFixtures.route1Summary))

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetRegionLocationsUseCase(FakeLocationRepository(locations = locations))

            assertEquals(locations.getValue("kanto"), useCase("kanto"))
        }

    @Test
    fun invoke_keepsPlacesWithHardlyAnyEncounters() =
        runTest {
            // Berry Forest has data in two of Kanto's twelve games, and that absence is what a
            // reader came to find out. Filtering it away would answer a question nobody asked.
            val useCase = GetRegionLocationsUseCase(FakeLocationRepository(locations = locations))

            assertTrue(useCase("kanto").any { it.slug == "berry-forest" })
        }

    @Test
    fun invoke_returnsEmptyForARegionWithNoRows() =
        runTest {
            val useCase = GetRegionLocationsUseCase(FakeLocationRepository(locations = locations))

            assertEquals(emptyList(), useCase("orre"))
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetRegionLocationsUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("kanto") }
        }
}
