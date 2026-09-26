package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
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

    /** A cell of the availability grid, including a grey one -- see #9 on confirming a negative. */
    data class LocationVersionSelected(
        val versionSlug: String,
    ) : DetailIntent

    /** Tapping the breadcrumb, which puts the grid back. */
    data object LocationVersionCleared : DetailIntent

    /** A route tapped in the Location tab. The game it was tapped under is already in the State. */
    data class PlaceClicked(
        val locationSlug: String,
    ) : DetailIntent

    data class MoveClicked(
        val slug: String,
    ) : DetailIntent

    data class AbilityClicked(
        val slug: String,
    ) : DetailIntent

    data object BackClicked : DetailIntent

    data object RetryClicked : DetailIntent
}

sealed interface DetailLabel {
    data object NavigateBack : DetailLabel

    /**
     * A move tapped in the Moves tab.
     *
     * Only the slug: a move has no artwork for its hero to open with, so unlike a dex card there is
     * nothing to hand forward. See #11 on why that transition is a push.
     */
    data class NavigateToMove(
        val slug: String,
    ) : DetailLabel

    /**
     * A route tapped in the Location tab, carrying the game it was tapped under.
     *
     * Both halves, because landing the reader back at the grid would ask them which game twice.
     * See #24 on closing the loop between the two views.
     */
    data class NavigateToLocation(
        val locationSlug: String,
        val versionSlug: String,
    ) : DetailLabel

    /** An ability tapped in the Moves tab. Only the slug, for the same reason. */
    data class NavigateToAbility(
        val slug: String,
    ) : DetailLabel
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
    data class MovesLoaded(
        val variantSlug: String,
        val moves: List<VariantMove>,
    ) : DetailMessage

    data class AvailabilityLoaded(
        val variantSlug: String,
        val availability: VariantAvailability,
    ) : DetailMessage

    data class LocationVersionSelected(
        val versionSlug: String,
    ) : DetailMessage

    data object LocationVersionCleared : DetailMessage

    data class VariantEncountersLoaded(
        val encounters: List<VariantEncounter>,
    ) : DetailMessage

    /** The other half of what the Moves tab shows, read in the same pass. */
    data class AbilitiesLoaded(
        val variantSlug: String,
        val abilities: List<VariantAbility>,
    ) : DetailMessage

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
    // Keyed by variant, because a form learns its own moves: Alolan Exeggutor is not Exeggutor with a
    // different colour. Read when the tab is first opened for a form rather than with the detail,
    // which is what keeps a reader who never opens it from paying for a hundred rows per swipe.
    val moves: Map<String, List<VariantMove>> = emptyMap(),
    // Keyed by variant for the same reason, and read in the same pass: a form has its own abilities
    // too, and Alolan Sandshrew's Slush Rush is not Sandshrew's Sand Veil.
    val abilities: Map<String, List<VariantAbility>> = emptyMap(),
    // Keyed by variant, because a form is not found where its base form is: Alolan Vulpix lives on a
    // different island from Vulpix. Read when the Location tab is first opened for a form.
    val availability: Map<String, VariantAvailability> = emptyMap(),
    // Only for the form and game on screen. Unlike the moves these are not worth keeping per form: a
    // reader who switches form is asking a different question, and the read is one cell wide.
    val locationVersion: String? = null,
    val isLoadingPlaces: Boolean = false,
    val places: List<VariantEncounter> = emptyList(),
    val tab: Tab = Tab.ABOUT,
    val error: AppError? = null,
) {
    enum class Tab {
        ABOUT,
        STATS,
        MOVES,
        LOCATION,
    }
}
