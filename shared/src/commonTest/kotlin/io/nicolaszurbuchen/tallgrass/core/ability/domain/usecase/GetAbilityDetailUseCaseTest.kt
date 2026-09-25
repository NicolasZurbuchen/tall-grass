package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.FakeAbilityRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GetAbilityDetailUseCaseTest {
    private val details = mapOf("levitate" to AbilityFixtures.levitateDetail)

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetAbilityDetailUseCase(FakeAbilityRepository(details = details))

            assertEquals(AbilityFixtures.levitateDetail, useCase("levitate"))
        }

    @Test
    fun invoke_returnsNullForASlugWithNoRow() =
        runTest {
            // Null rather than an exception: the Store turns it into the not-found error, which is a
            // different thing from the read having failed.
            val useCase = GetAbilityDetailUseCase(FakeAbilityRepository(details = details))

            assertNull(useCase("missingno"))
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningNull() =
        runTest {
            // Null already means "no such ability", so a broken read must not borrow it.
            val useCase = GetAbilityDetailUseCase(FakeAbilityRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("levitate") }
        }
}
