package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One of the three figures at the top of a move: power, accuracy, PP.
 *
 * Already formatted, dash and all. 331 moves have no power and 285 cannot miss, and deciding what
 * that looks like is a mapping decision rather than one to repeat at the call site.
 */
@Immutable
data class MoveStatUiModel(
    val label: UiText,
    val valueText: String,
)
