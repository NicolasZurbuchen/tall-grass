package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetEncounterConditionsUseCaseTest {
    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetEncounterConditionsUseCase(FakeLocationRepository(conditions = LocationFixtures.timeOfDay))

            assertEquals(LocationFixtures.timeOfDay, useCase())
        }

    @Test
    fun invoke_carriesTheAxisEachValueBelongsTo() =
        runTest {
            // The selector draws one control per axis, so a value that arrived without one would
            // have nowhere to go.
            val useCase = GetEncounterConditionsUseCase(FakeLocationRepository(conditions = LocationFixtures.timeOfDay))

            assertEquals(setOf("time"), useCase().map { it.axis }.toSet())
            assertEquals(1, useCase().count { it.isDefault })
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetEncounterConditionsUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase() }
        }
}
