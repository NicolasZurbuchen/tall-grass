package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovesUiMapperTest {
    @Test
    fun toUiModel_mapsTheCardsWhenTheCallerDoesNotSupplyThem() {
        val state = MovesState(isLoading = false, moves = listOf(MoveFixtures.flamethrower))

        val model = state.toUiModel()

        assertEquals(listOf("flamethrower"), model.moves.map { it.slug })
        assertEquals(false, model.isLoading)
        assertNull(model.error)
    }

    @Test
    fun toUiModel_usesTheCardsTheCallerSuppliesRatherThanMappingAgain() {
        // The ViewModel holds onto its cards across states that do not change them, which is the
        // whole reason this parameter exists. Passing an empty list where the state has a move is
        // the only way to see which one was used.
        val state = MovesState(isLoading = false, moves = listOf(MoveFixtures.flamethrower))

        assertTrue(state.toUiModel(moves = emptyList()).moves.isEmpty())
    }

    @Test
    fun toUiModel_carriesTheErrorThrough() {
        val state = MovesState(isLoading = false, error = AppError.Database.NotFound)

        assertTrue(state.toUiModel().error != null)
    }
}
