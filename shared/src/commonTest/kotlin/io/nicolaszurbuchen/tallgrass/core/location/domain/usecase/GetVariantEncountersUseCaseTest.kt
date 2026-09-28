package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetVariantEncountersUseCaseTest {
    private val rows = mapOf(("pidgey" to "heartgold") to listOf(LocationFixtures.pidgeyOnRoute1))

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetVariantEncountersUseCase(FakeLocationRepository(encountersFor = rows))

            assertEquals(listOf(LocationFixtures.pidgeyOnRoute1), useCase("pidgey", "heartgold"))
        }

    @Test
    fun invoke_readsTheSameRowsTheOtherWayRound() =
        runTest {
            // The route side carries the Pokemon and this side carries the place. #24 noticed the
            // two screens are one component pointed in two directions, and this is that turn.
            val useCase = GetVariantEncountersUseCase(FakeLocationRepository(encountersFor = rows))

            assertEquals("kanto-route-1", useCase("pidgey", "heartgold").single().locationSlug)
        }

    @Test
    fun invoke_returnsEmptyForAGameItIsNotIn() =
        runTest {
            val useCase = GetVariantEncountersUseCase(FakeLocationRepository(encountersFor = rows))

            assertEquals(emptyList(), useCase("pidgey", "red"))
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetVariantEncountersUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("pidgey", "heartgold") }
        }
}
