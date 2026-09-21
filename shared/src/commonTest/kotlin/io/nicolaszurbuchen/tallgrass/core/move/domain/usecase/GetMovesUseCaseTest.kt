package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetMovesUseCaseTest {
    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val moves = listOf(MoveFixtures.flamethrower, MoveFixtures.thunderWave)
            val useCase = GetMovesUseCase(FakeMoveRepository(moves = moves))

            assertEquals(moves, useCase())
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningEmpty() =
        runTest {
            // An empty list and a broken read look identical to the screen, so the use case must not
            // flatten one into the other.
            val useCase = GetMovesUseCase(FakeMoveRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase() }
        }
}
