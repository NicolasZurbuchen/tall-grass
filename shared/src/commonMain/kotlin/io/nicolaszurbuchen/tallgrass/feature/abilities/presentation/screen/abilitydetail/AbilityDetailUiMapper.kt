package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityDetailTabUiModel

fun AbilityDetailState.toUiModel(): AbilityDetailUiModel =
    AbilityDetailUiModel(
        isLoading = isLoading,
        ability = ability?.toUiModel(holders),
        holders = holders.map { it.toUiModel() },
        tab =
            when (tab) {
                AbilityDetailState.Tab.DETAILS -> AbilityDetailTabUiModel.DETAILS
                AbilityDetailState.Tab.HOLDERS -> AbilityDetailTabUiModel.HOLDERS
            },
        error = error?.toUiModel(),
    )
