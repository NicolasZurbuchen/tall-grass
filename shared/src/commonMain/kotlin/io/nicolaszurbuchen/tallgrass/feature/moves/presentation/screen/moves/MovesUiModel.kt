package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel

@Immutable
data class MovesUiModel(
    val isLoading: Boolean,
    val moves: List<MoveUiModel>,
    val error: AppErrorUiModel?,
)

/**
 * One card in the moves list.
 *
 * [type] carries both the name on the pill and the colour of the card, which is the same thing a dex
 * card does with its primary type.
 *
 * [powerText] is already formatted, dash and all: 331 moves have no power, and deciding what that
 * looks like is a mapping decision rather than one to repeat at the call site.
 */
@Immutable
data class MoveUiModel(
    val slug: String,
    val name: String,
    val type: TypeUiModel,
    val damageClass: DamageClassUiModel,
    val powerText: String,
)
