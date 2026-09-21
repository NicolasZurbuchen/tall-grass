package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.LearnMethodUiModel

/** Exhaustive by construction: a fifth method breaks this at compile time. */
fun LearnMethod.toUiModel(): LearnMethodUiModel =
    when (this) {
        LearnMethod.LEVEL_UP -> LearnMethodUiModel.LEVEL_UP
        LearnMethod.MACHINE -> LearnMethodUiModel.MACHINE
        LearnMethod.EGG -> LearnMethodUiModel.EGG
        LearnMethod.TUTOR -> LearnMethodUiModel.TUTOR
    }
