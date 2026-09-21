package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveTarget
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.MoveTargetUiModel

/** Exhaustive by construction: both enums list the same sixteen members. */
fun MoveTarget.toUiModel(): MoveTargetUiModel =
    when (this) {
        MoveTarget.SPECIFIC_MOVE -> MoveTargetUiModel.SPECIFIC_MOVE
        MoveTarget.SELECTED_POKEMON_ME_FIRST -> MoveTargetUiModel.SELECTED_POKEMON_ME_FIRST
        MoveTarget.ALLY -> MoveTargetUiModel.ALLY
        MoveTarget.USERS_FIELD -> MoveTargetUiModel.USERS_FIELD
        MoveTarget.USER_OR_ALLY -> MoveTargetUiModel.USER_OR_ALLY
        MoveTarget.OPPONENTS_FIELD -> MoveTargetUiModel.OPPONENTS_FIELD
        MoveTarget.USER -> MoveTargetUiModel.USER
        MoveTarget.RANDOM_OPPONENT -> MoveTargetUiModel.RANDOM_OPPONENT
        MoveTarget.ALL_OTHER_POKEMON -> MoveTargetUiModel.ALL_OTHER_POKEMON
        MoveTarget.SELECTED_POKEMON -> MoveTargetUiModel.SELECTED_POKEMON
        MoveTarget.ALL_OPPONENTS -> MoveTargetUiModel.ALL_OPPONENTS
        MoveTarget.ENTIRE_FIELD -> MoveTargetUiModel.ENTIRE_FIELD
        MoveTarget.USER_AND_ALLIES -> MoveTargetUiModel.USER_AND_ALLIES
        MoveTarget.ALL_POKEMON -> MoveTargetUiModel.ALL_POKEMON
        MoveTarget.ALL_ALLIES -> MoveTargetUiModel.ALL_ALLIES
        MoveTarget.FAINTING_POKEMON -> MoveTargetUiModel.FAINTING_POKEMON
    }
