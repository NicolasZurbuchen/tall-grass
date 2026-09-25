package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.FakeAbilityRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetAbilitiesUseCaseTest {
    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val abilities = listOf(AbilityFixtures.adaptability, AbilityFixtures.levitate)
            val useCase = GetAbilitiesUseCase(FakeAbilityRepository(abilities = abilities))

            assertEquals(abilities, useCase())
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningEmpty() =
        runTest {
            // An empty list and a broken read look identical to the screen, so the use case must not
            // flatten one into the other.
            val useCase = GetAbilitiesUseCase(FakeAbilityRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase() }
        }
}
