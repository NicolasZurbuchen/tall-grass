package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovesReducerTest {
    private val reduce = MovesStoreFactory.ReducerImpl

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            // Otherwise a retry would show the skeleton and the old error banner at once.
            val state =
                MovesState(error = AppError.Unexpected(IllegalStateException())).reduce(MovesMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun movesLoaded_stopsLoadingAndHoldsTheMoves() =
        with(reduce) {
            val moves = listOf(MoveFixtures.flamethrower, MoveFixtures.thunderWave)

            val state = MovesState(isLoading = true).reduce(MovesMessage.MovesLoaded(moves))

            assertEquals(moves, state.moves)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun movesLoaded_handsBackTheSameListInstanceItWasGiven() =
        with(reduce) {
            // Identity is what MovesViewModel keys its card cache on, so a reducer that copied the
            // list would silently remap 919 cards on every state.
            val moves = listOf(MoveFixtures.flamethrower)

            val state = MovesState().reduce(MovesMessage.MovesLoaded(moves))

            assertTrue(state.moves === moves)
        }

    @Test
    fun loadFailed_stopsLoadingAndKeepsWhatWasAlreadyShown() =
        with(reduce) {
            val loaded = MovesState(moves = listOf(MoveFixtures.flamethrower), isLoading = false)

            val state = loaded.reduce(MovesMessage.LoadFailed(AppError.Unexpected(IllegalStateException())))

            assertEquals(listOf(MoveFixtures.flamethrower), state.moves)
            assertEquals(false, state.isLoading)
            assertTrue(state.error != null)
        }
}
