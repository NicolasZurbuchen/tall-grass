package io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.uimodel.GrowthRateUiModel

fun GrowthRate.toUiModel(): GrowthRateUiModel =
    when (this) {
        GrowthRate.SLOW -> GrowthRateUiModel.SLOW
        GrowthRate.MEDIUM_SLOW -> GrowthRateUiModel.MEDIUM_SLOW
        GrowthRate.MEDIUM_FAST -> GrowthRateUiModel.MEDIUM_FAST
        GrowthRate.FAST -> GrowthRateUiModel.FAST
        GrowthRate.ERRATIC -> GrowthRateUiModel.ERRATIC
        GrowthRate.FLUCTUATING -> GrowthRateUiModel.FLUCTUATING
    }
