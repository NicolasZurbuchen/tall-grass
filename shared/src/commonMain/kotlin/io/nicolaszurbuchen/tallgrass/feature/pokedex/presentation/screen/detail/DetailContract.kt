package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup

sealed interface DetailIntent {
    data class FormSelected(
        val variantSlug: String,
    ) : DetailIntent

    data class TabSelected(
        val tab: DetailState.Tab,
    ) : DetailIntent

    data object BackClicked : DetailIntent

    data object RetryClicked : DetailIntent
}

sealed interface DetailLabel {
    data object NavigateBack : DetailLabel
}

sealed interface DetailAction {
    data object LoadDetail : DetailAction
}

sealed interface DetailMessage {
    data object LoadStarted : DetailMessage

    /**
     * [matchups] is keyed by variant slug and covers every form, because it is read once with the
     * detail. Switching form must not wait on the database.
     */
    data class DetailLoaded(
        val detail: PokemonDetail,
        val matchups: Map<String, List<TypeMatchup>>,
    ) : DetailMessage

    data class LoadFailed(
        val error: AppError,
    ) : DetailMessage

    data class FormSwitched(
        val variantSlug: String,
    ) : DetailMessage

    data class TabSwitched(
        val tab: DetailState.Tab,
    ) : DetailMessage
}

/**
 * [entryVariantSlug] is the form that was tapped and never changes. [activeVariantSlug] is the form
 * on screen and does — the two differ as soon as the switcher is used, and the difference is what
 * decides whether the hero still owns the shared element it arrived with.
 *
 * [Tab] is nested rather than a type of its own because a Contract holds the Store's vocabulary and
 * nothing else. Which tab is open is state with no domain behind it, and it has no business being a
 * UiModel the Store would then be carrying.
 */
data class DetailState(
    val entryVariantSlug: String,
    val isLoading: Boolean = true,
    val detail: PokemonDetail? = null,
    val activeVariantSlug: String = entryVariantSlug,
    val matchups: Map<String, List<TypeMatchup>> = emptyMap(),
    val tab: Tab = Tab.ABOUT,
    val error: AppError? = null,
) {
    enum class Tab {
        ABOUT,
        STATS,
    }
}
