package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One move, ready to draw.
 *
 * [effect] is null for the 93 Generation VIII and IX moves upstream has written no prose for, and the
 * section is left out rather than filled — see `DECISIONS.md § A move's absent numbers are absent,
 * not zero`. [facts] is empty for the 92 with no meta row, very nearly the same list.
 */
@Immutable
data class MoveContentUiModel(
    val name: String,
    val type: TypeUiModel,
    val damageClass: DamageClassUiModel,
    val ppText: UiText?,
    val bars: List<MoveBarUiModel>,
    val effect: String?,
    val targetText: UiText,
    val priorityText: String,
    val facts: List<MoveFactUiModel>,
)
