package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One lane in the Battle Data block.
 *
 * [fraction] is 0 where the figure is absent, which is what draws an empty lane beside a dash rather
 * than a full one beside nothing.
 */
@Immutable
data class MoveBarUiModel(
    val label: UiText,
    val valueText: String,
    val fraction: Float,
)
