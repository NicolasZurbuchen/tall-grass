package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoveDetailUiMapperTest {
    @Test
    fun toUiModel_hasNothingToDrawWhileTheReadIsInFlight() {
        // The screen opens on a skeleton rather than on a half-drawn hero, because a move card hands
        // nothing forward -- not even the colour. See #11 on why this transition is a push.
        val model = MoveDetailState(isLoading = true).toUiModel()

        assertTrue(model.isLoading)
        assertNull(model.move)
    }

    @Test
    fun toUiModel_carriesTheLoadedMoveThrough() {
        val state = MoveDetailState(isLoading = false, move = MoveFixtures.flamethrowerDetail)

        assertEquals("Flamethrower", state.toUiModel().move?.name)
    }

    @Test
    fun toUiModel_carriesTheErrorThrough() {
        val state = MoveDetailState(isLoading = false, error = AppError.Database.NotFound)

        assertTrue(state.toUiModel().error != null)
    }
}
