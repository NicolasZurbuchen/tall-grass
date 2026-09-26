package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GetLocationDetailUseCaseTest {
    private val details = mapOf("kanto-route-1" to LocationFixtures.route1)

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetLocationDetailUseCase(FakeLocationRepository(locationDetails = details))

            assertEquals(LocationFixtures.route1, useCase("kanto-route-1"))
        }

    @Test
    fun invoke_keepsTheGamesWithNothingInThem() =
        runTest {
            // The grid draws a cell for every game of the region and only some come up green. A grey
            // cell has to be there to be tapped, which is how a reader confirms a negative -- see #9.
            val useCase = GetLocationDetailUseCase(FakeLocationRepository(locationDetails = details))
            val route = useCase("kanto-route-1")

            assertEquals(listOf("heartgold", "red"), route?.versions?.map { it.slug })
            assertEquals(setOf("heartgold"), route?.encounteredVersions)
        }

    @Test
    fun invoke_returnsNullForASlugWithNoRow() =
        runTest {
            val useCase = GetLocationDetailUseCase(FakeLocationRepository(locationDetails = details))

            assertNull(useCase("kanto-route-99"))
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningNull() =
        runTest {
            val useCase = GetLocationDetailUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("kanto-route-1") }
        }
}
