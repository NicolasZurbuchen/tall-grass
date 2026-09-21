package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.mapper.toUiModel

/**
 * [moves] is mapped from the State by default, which is what every caller but one wants.
 *
 * `MovesViewModel` passes its own, on the same grounds as `DexViewModel`: mapping 919 cards is the
 * expensive half of this function.
 */
fun MovesState.toUiModel(moves: List<MoveUiModel> = this.moves.map { it.toUiModel() }): MovesUiModel =
    MovesUiModel(
        isLoading = isLoading,
        moves = moves,
        error = error?.toUiModel(),
    )
