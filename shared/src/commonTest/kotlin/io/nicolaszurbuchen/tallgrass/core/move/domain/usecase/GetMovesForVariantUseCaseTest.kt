package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetMovesForVariantUseCaseTest {
    private val moves = mapOf("charizard" to listOf(MoveFixtures.charizardFlamethrower))

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetMovesForVariantUseCase(FakeMoveRepository(variantMoves = moves))

            assertEquals(listOf(MoveFixtures.charizardFlamethrower), useCase("charizard"))
        }

    @Test
    fun invoke_returnsEmptyForAFormThatLearnsNothingOfItsOwn() =
        runTest {
            // Every Mega and Gigantamax: they learn what their base form learns and upstream does not
            // repeat the rows. An empty list is the answer rather than the absence of one.
            val useCase = GetMovesForVariantUseCase(FakeMoveRepository(variantMoves = moves))

            assertTrue(useCase("charizard-mega-x").isEmpty())
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetMovesForVariantUseCase(FakeMoveRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("charizard") }
        }
}
