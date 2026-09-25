package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.error.AppError

sealed interface AbilitiesIntent {
    data class AbilityClicked(
        val slug: String,
    ) : AbilitiesIntent

    data object RetryClicked : AbilitiesIntent
}

sealed interface AbilitiesLabel {
    data class NavigateToDetail(
        val slug: String,
    ) : AbilitiesLabel
}

sealed interface AbilitiesAction {
    data object LoadAbilities : AbilitiesAction
}

sealed interface AbilitiesMessage {
    data object LoadStarted : AbilitiesMessage

    data class AbilitiesLoaded(
        val abilities: List<Ability>,
    ) : AbilitiesMessage

    data class LoadFailed(
        val error: AppError,
    ) : AbilitiesMessage
}

data class AbilitiesState(
    val isLoading: Boolean = true,
    val abilities: List<Ability> = emptyList(),
    val error: AppError? = null,
)
