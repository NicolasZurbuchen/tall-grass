package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.CaptureMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetVariantAvailabilityUseCaseTest {
    private val availability = mapOf("pidgey" to LocationFixtures.pidgeyAvailability)

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetVariantAvailabilityUseCase(FakeLocationRepository(availability = availability))

            assertEquals(LocationFixtures.pidgeyAvailability, useCase("pidgey"))
        }

    @Test
    fun invoke_spansEveryGameRatherThanOneRegion() =
        runTest {
            // This side of the grid is not scoped to a region: the question is which of my games has
            // this, and the answer crosses all of them. Pokemon Y is in the list although Pidgey is
            // not in it, because the cell has to be there to be tapped.
            val useCase = GetVariantAvailabilityUseCase(FakeLocationRepository(availability = availability))
            val found = useCase("pidgey")

            assertTrue("y" in found.versions.map { it.slug })
            assertTrue("y" !in found.encounteredVersions)
        }

    @Test
    fun invoke_returnsAnEmptySummaryRatherThanNull() =
        runTest {
            // A Pokemon nothing has ever met still has an answer: no pills, no green cells. Null
            // would make the tab indistinguishable from a read that had not finished.
            val useCase = GetVariantAvailabilityUseCase(FakeLocationRepository(availability = availability))
            val found = useCase("missingno")

            assertEquals(emptyList<CaptureMethod>(), found.captureMethods)
            assertEquals(emptySet(), found.encounteredVersions)
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetVariantAvailabilityUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("pidgey") }
        }
}
