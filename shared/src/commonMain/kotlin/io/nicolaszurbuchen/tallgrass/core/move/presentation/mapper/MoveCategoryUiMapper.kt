package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveCategory
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.MoveCategoryUiModel

/** Exhaustive by construction: both enums list the same fourteen members. */
fun MoveCategory.toUiModel(): MoveCategoryUiModel =
    when (this) {
        MoveCategory.DAMAGE -> MoveCategoryUiModel.DAMAGE
        MoveCategory.AILMENT -> MoveCategoryUiModel.AILMENT
        MoveCategory.NET_GOOD_STATS -> MoveCategoryUiModel.NET_GOOD_STATS
        MoveCategory.HEAL -> MoveCategoryUiModel.HEAL
        MoveCategory.DAMAGE_AILMENT -> MoveCategoryUiModel.DAMAGE_AILMENT
        MoveCategory.SWAGGER -> MoveCategoryUiModel.SWAGGER
        MoveCategory.DAMAGE_LOWER -> MoveCategoryUiModel.DAMAGE_LOWER
        MoveCategory.DAMAGE_RAISE -> MoveCategoryUiModel.DAMAGE_RAISE
        MoveCategory.DAMAGE_HEAL -> MoveCategoryUiModel.DAMAGE_HEAL
        MoveCategory.OHKO -> MoveCategoryUiModel.OHKO
        MoveCategory.WHOLE_FIELD_EFFECT -> MoveCategoryUiModel.WHOLE_FIELD_EFFECT
        MoveCategory.FIELD_EFFECT -> MoveCategoryUiModel.FIELD_EFFECT
        MoveCategory.FORCE_SWITCH -> MoveCategoryUiModel.FORCE_SWITCH
        MoveCategory.UNIQUE -> MoveCategoryUiModel.UNIQUE
    }
