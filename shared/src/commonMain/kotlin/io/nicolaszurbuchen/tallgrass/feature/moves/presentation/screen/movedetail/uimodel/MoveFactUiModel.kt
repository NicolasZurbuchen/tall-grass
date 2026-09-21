package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One line of the Mechanics block: a fact upstream recorded about how the move behaves.
 *
 * A list rather than a record with a field per mechanic, because a move has between zero and ten of
 * these and which ones it has is the interesting part. A record would make the screen ask ten
 * questions to find the two that have answers.
 */
@Immutable
data class MoveFactUiModel(
    val label: UiText,
    val value: UiText,
)
