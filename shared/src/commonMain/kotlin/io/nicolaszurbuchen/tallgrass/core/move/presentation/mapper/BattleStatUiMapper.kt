package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.BattleStat
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.BattleStatUiModel

/** Exhaustive by construction: both enums list the same seven members, in the same order. */
fun BattleStat.toUiModel(): BattleStatUiModel =
    when (this) {
        BattleStat.ATTACK -> BattleStatUiModel.ATTACK
        BattleStat.DEFENSE -> BattleStatUiModel.DEFENSE
        BattleStat.SPECIAL_ATTACK -> BattleStatUiModel.SPECIAL_ATTACK
        BattleStat.SPECIAL_DEFENSE -> BattleStatUiModel.SPECIAL_DEFENSE
        BattleStat.SPEED -> BattleStatUiModel.SPEED
        BattleStat.ACCURACY -> BattleStatUiModel.ACCURACY
        BattleStat.EVASION -> BattleStatUiModel.EVASION
    }
