package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetMoveLearnersUseCaseTest {
    private val learners = mapOf("flamethrower" to listOf(MoveFixtures.charizardLearner))

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetMoveLearnersUseCase(FakeMoveRepository(learners = learners))

            assertEquals(listOf(MoveFixtures.charizardLearner), useCase("flamethrower"))
        }

    @Test
    fun invoke_returnsEmptyForAMoveNobodyLearns() =
        runTest {
            // 106 moves are in this state -- Z-moves, Max moves and the battle-only ones -- and an
            // empty list is the answer rather than the absence of one.
            val useCase = GetMoveLearnersUseCase(FakeMoveRepository(learners = learners))

            assertTrue(useCase("acid-downpour--physical").isEmpty())
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetMoveLearnersUseCase(FakeMoveRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("flamethrower") }
        }
}
