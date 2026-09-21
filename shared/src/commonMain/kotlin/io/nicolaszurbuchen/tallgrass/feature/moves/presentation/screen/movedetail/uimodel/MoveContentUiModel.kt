package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel

/**
 * One move, ready to draw, in the order it is read: the three figures that decide whether to use it,
 * the sentence that says what it does, and the detail behind the sentence.
 *
 * [effect] is null for the 93 Generation VIII and IX moves upstream has written no prose for, and the
 * section is left out rather than filled — see `DECISIONS.md § A move's absent numbers are absent,
 * not zero`. [facts] always holds the move's target and priority; everything after those is there
 * only when upstream recorded it, and for the 92 moves with no meta row that is nothing at all.
 */
@Immutable
data class MoveContentUiModel(
    val name: String,
    val type: TypeUiModel,
    val damageClass: DamageClassUiModel,
    val stats: List<MoveStatUiModel>,
    val effect: String?,
    val facts: List<MoveFactUiModel>,
)
