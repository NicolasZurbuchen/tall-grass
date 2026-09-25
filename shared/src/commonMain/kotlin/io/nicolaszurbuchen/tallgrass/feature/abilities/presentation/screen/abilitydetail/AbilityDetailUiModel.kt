package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityHolderUiModel

@Immutable
data class AbilityDetailUiModel(
    val isLoading: Boolean,
    val ability: AbilityContentUiModel?,
    val holders: List<AbilityHolderUiModel>,
    val tab: AbilityDetailTabUiModel,
    val error: AppErrorUiModel?,
)
