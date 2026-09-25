package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.FakeAbilityRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetAbilityHoldersUseCaseTest {
    private val holders = mapOf("levitate" to listOf(AbilityFixtures.gastly, AbilityFixtures.vibrava))

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetAbilityHoldersUseCase(FakeAbilityRepository(holders = holders))

            assertEquals(listOf(AbilityFixtures.gastly, AbilityFixtures.vibrava), useCase("levitate"))
        }

    @Test
    fun invoke_returnsEmptyForAnAbilityNothingHas() =
        runTest {
            // An empty list is an answer here rather than a miss, which is why the Known by tab says
            // so out loud instead of showing a spinner.
            val useCase = GetAbilityHoldersUseCase(FakeAbilityRepository(holders = holders))

            assertTrue(useCase("stench").isEmpty())
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningEmpty() =
        runTest {
            val useCase = GetAbilityHoldersUseCase(FakeAbilityRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("levitate") }
        }
}
