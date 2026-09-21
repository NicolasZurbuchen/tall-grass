package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoveDetailReducerTest {
    private val reduce = MoveDetailStoreFactory.ReducerImpl

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            val state = MoveDetailState(error = AppError.Database.NotFound).reduce(MoveDetailMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun moveLoaded_stopsLoadingAndHoldsTheMove() =
        with(reduce) {
            val state =
                MoveDetailState(isLoading = true)
                    .reduce(MoveDetailMessage.MoveLoaded(MoveFixtures.flamethrowerDetail, emptyList()))

            assertEquals(MoveFixtures.flamethrowerDetail, state.move)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun loadFailed_stopsLoadingAndKeepsWhatWasAlreadyShown() =
        with(reduce) {
            // A failed retry over a move already on screen should not blank it.
            val loaded = MoveDetailState(move = MoveFixtures.flamethrowerDetail, isLoading = false)

            val state = loaded.reduce(MoveDetailMessage.LoadFailed(AppError.Database.NotFound))

            assertEquals(MoveFixtures.flamethrowerDetail, state.move)
            assertEquals(false, state.isLoading)
            assertTrue(state.error != null)
        }
}
