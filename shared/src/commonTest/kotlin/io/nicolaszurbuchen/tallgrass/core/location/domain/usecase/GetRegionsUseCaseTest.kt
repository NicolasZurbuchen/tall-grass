package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetRegionsUseCaseTest {
    private val regions = listOf(LocationFixtures.kanto, LocationFixtures.orre)

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetRegionsUseCase(FakeLocationRepository(regions = regions))

            assertEquals(regions, useCase())
        }

    @Test
    fun invoke_keepsTheRepositoryOrder() =
        runTest {
            // The list runs in release order with the spin-off last, and that order is the
            // repository's to decide -- re-sorting here would put Orre beside Hoenn.
            val useCase = GetRegionsUseCase(FakeLocationRepository(regions = regions))

            assertEquals(listOf("kanto", "orre"), useCase().map { it.slug })
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetRegionsUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase() }
        }
}
