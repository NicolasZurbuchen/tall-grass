package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery

sealed interface DetailIntent {
    data class FormSelected(
        val variantSlug: String,
    ) : DetailIntent

    /** The carousel moved onto another card. Not a form switch: this is a different Pokemon. */
    data class EntrySelected(
        val entrySlug: String,
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
    data object LoadCarousel : DetailAction

    data object LoadDetail : DetailAction
}

sealed interface DetailMessage {
    data object LoadStarted : DetailMessage

    /**
     * The list the detail was opened from, which the carousel swipes along. Empty when the read
     * failed: the screen is about one Pokemon and still works without its neighbours.
     */
    data class CarouselLoaded(
        val entries: List<DexEntry>,
    ) : DetailMessage

    /**
     * [entrySlug] is which card this answers for, because a read for a neighbour can land while the
     * reader is somewhere else and must not overwrite what they are looking at.
     *
     * [matchups] is keyed by variant slug and covers every form, because it is read once with the
     * detail. Switching form must not wait on the database.
     */
    data class DetailLoaded(
        val entrySlug: String,
        val detail: PokemonDetail,
        val matchups: Map<String, List<TypeMatchup>>,
    ) : DetailMessage

    data class LoadFailed(
        val error: AppError,
    ) : DetailMessage

    data class EntrySwitched(
        val entrySlug: String,
    ) : DetailMessage

    data class FormSwitched(
        val variantSlug: String,
    ) : DetailMessage

    data class TabSwitched(
        val tab: DetailState.Tab,
    ) : DetailMessage
}

/**
 * Three slugs, and they are not the same question.
 *
 * [entryVariantSlug] is the card that was tapped and never changes — it is what decides whether the
 * hero still owns the shared element it arrived with. [activeEntrySlug] is the card the carousel is
 * on, which the swipe moves. [activeVariantSlug] is the form on screen, which the switcher moves and
 * which a swipe resets to the new card's own form.
 *
 * [entries] is the list the carousel walks, read for [query]. Empty until it lands, and empty for
 * good if it fails — see [DetailMessage.CarouselLoaded].
 *
 * [details] holds the card on screen and the two either side of it, so a swipe onto a card that has
 * already been read shows it without passing through a skeleton. See
 * `DECISIONS.md § The carousel reads ahead, so a swipe lands on content`.
 *
 * [Tab] is nested rather than a type of its own because a Contract holds the Store's vocabulary and
 * nothing else. Which tab is open is state with no domain behind it, and it has no business being a
 * UiModel the Store would then be carrying.
 */
data class DetailState(
    val entryVariantSlug: String,
    val query: DexQuery,
    val entries: List<DexEntry> = emptyList(),
    val activeEntrySlug: String = entryVariantSlug,
    val isLoading: Boolean = true,
    val details: Map<String, PokemonDetail> = emptyMap(),
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
