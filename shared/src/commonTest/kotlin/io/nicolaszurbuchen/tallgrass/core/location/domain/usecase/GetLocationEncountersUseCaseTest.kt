package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetLocationEncountersUseCaseTest {
    private val rows =
        mapOf(
            ("kanto-route-1" to "heartgold") to listOf(LocationFixtures.pidgeyByDay, LocationFixtures.rattataByNight),
        )

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetLocationEncountersUseCase(FakeLocationRepository(encountersAt = rows))

            assertEquals(rows.getValue("kanto-route-1" to "heartgold"), useCase("kanto-route-1", "heartgold"))
        }

    @Test
    fun invoke_answersPerGameRatherThanPerPlace() =
        runTest {
            // Route 1 means a different table in every game it appears in, which is the one thing
            // the app cannot be game-agnostic about. See #9.
            val useCase = GetLocationEncountersUseCase(FakeLocationRepository(encountersAt = rows))

            assertEquals(emptyList(), useCase("kanto-route-1", "red"))
        }

    @Test
    fun invoke_keepsEveryRowOfAConditionedTable() =
        runTest {
            // Two rows of one table, each tagged with the state that switches it on. Collapsing them
            // to one rate is the mistake the correction on #8 exists to prevent, and it cannot be
            // undone downstream.
            val useCase = GetLocationEncountersUseCase(FakeLocationRepository(encountersAt = rows))
            val table = useCase("kanto-route-1", "heartgold")

            assertEquals(listOf(listOf("time-day"), listOf("time-night")), table.map { it.conditions })
            assertEquals(75, table.sumOf { it.chance })
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetLocationEncountersUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("kanto-route-1", "heartgold") }
        }
}
