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
    fun invoke_returnsEmptyForAVariantWithNoRows() =
        runTest {
            // No form in the dataset is like this any more: the ones upstream files no rows for borrow
            // their base form's. The use case must still report what the database holds rather than
            // reach for a base form of its own, which is a decision taken once at generation time.
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
