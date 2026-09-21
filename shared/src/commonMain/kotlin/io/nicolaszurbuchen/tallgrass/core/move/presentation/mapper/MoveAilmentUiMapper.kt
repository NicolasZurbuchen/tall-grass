package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveAilment
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.MoveAilmentUiModel

/** Exhaustive by construction: both enums list the same twenty-two members. */
fun MoveAilment.toUiModel(): MoveAilmentUiModel =
    when (this) {
        MoveAilment.PARALYSIS -> MoveAilmentUiModel.PARALYSIS
        MoveAilment.SLEEP -> MoveAilmentUiModel.SLEEP
        MoveAilment.FREEZE -> MoveAilmentUiModel.FREEZE
        MoveAilment.BURN -> MoveAilmentUiModel.BURN
        MoveAilment.POISON -> MoveAilmentUiModel.POISON
        MoveAilment.CONFUSION -> MoveAilmentUiModel.CONFUSION
        MoveAilment.INFATUATION -> MoveAilmentUiModel.INFATUATION
        MoveAilment.TRAP -> MoveAilmentUiModel.TRAP
        MoveAilment.NIGHTMARE -> MoveAilmentUiModel.NIGHTMARE
        MoveAilment.TORMENT -> MoveAilmentUiModel.TORMENT
        MoveAilment.DISABLE -> MoveAilmentUiModel.DISABLE
        MoveAilment.YAWN -> MoveAilmentUiModel.YAWN
        MoveAilment.HEAL_BLOCK -> MoveAilmentUiModel.HEAL_BLOCK
        MoveAilment.NO_TYPE_IMMUNITY -> MoveAilmentUiModel.NO_TYPE_IMMUNITY
        MoveAilment.LEECH_SEED -> MoveAilmentUiModel.LEECH_SEED
        MoveAilment.EMBARGO -> MoveAilmentUiModel.EMBARGO
        MoveAilment.PERISH_SONG -> MoveAilmentUiModel.PERISH_SONG
        MoveAilment.INGRAIN -> MoveAilmentUiModel.INGRAIN
        MoveAilment.SILENCE -> MoveAilmentUiModel.SILENCE
        MoveAilment.TAR_SHOT -> MoveAilmentUiModel.TAR_SHOT
        MoveAilment.PROTECT -> MoveAilmentUiModel.PROTECT
        MoveAilment.UNKNOWN -> MoveAilmentUiModel.UNKNOWN
    }
