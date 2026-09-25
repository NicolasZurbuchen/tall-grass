package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.error.AppError

sealed interface AbilityDetailIntent {
    data class TabSelected(
        val tab: AbilityDetailState.Tab,
    ) : AbilityDetailIntent

    data class HolderClicked(
        val variantSlug: String,
    ) : AbilityDetailIntent

    data object RetryClicked : AbilityDetailIntent
}

/**
 * The one thing this screen publishes outward: a tap on a Pokemon in the Known by tab.
 *
 * It carries what the Pokemon's hero opens with rather than only the slug, for the same reason a dex
 * card's tap does -- the hero draws the artwork and the colour on its first frame, and this list has
 * both. See `HeroHandoff` in the Pokedex feature, which this cannot name: a feature may only import
 * from its own subtree, so `app/` is where these become a destination.
 *
 * [cardSlug] and [formSlug] are the same string for most Pokemon and not for any variant: only default
 * forms are dex cards, so Alolan Sandshrew is reached through Sandshrew. The card is what the detail's
 * carousel swipes along and the form is what it opens on.
 */
sealed interface AbilityDetailLabel {
    data class NavigateToPokemon(
        val cardSlug: String,
        val formSlug: String,
        val name: String,
        val artworkUrl: String,
        val primaryTypeSlug: String,
        val secondaryTypeSlug: String?,
    ) : AbilityDetailLabel
}

sealed interface AbilityDetailAction {
    data object LoadAbility : AbilityDetailAction
}

sealed interface AbilityDetailMessage {
    data object LoadStarted : AbilityDetailMessage

    data class AbilityLoaded(
        val ability: AbilityDetail,
        val holders: List<AbilityHolder>,
    ) : AbilityDetailMessage

    data class LoadFailed(
        val error: AppError,
    ) : AbilityDetailMessage

    data class TabChanged(
        val tab: AbilityDetailState.Tab,
    ) : AbilityDetailMessage
}

/**
 * [ability] is null until the read lands. There is nothing to draw before then: an ability card hands
 * nothing forward, because the transition into this screen is a push rather than a shared element --
 * see `AbilityDetailDestination`.
 *
 * [holders] arrives with the ability rather than when its tab is opened. The two reads are one round
 * trip to the same local database, and a tab that populates a beat after it is tapped reads as slower
 * than one that was always ready. They are also what the Details tab counts, so the first tab cannot
 * draw itself without them.
 */
data class AbilityDetailState(
    val isLoading: Boolean = true,
    val ability: AbilityDetail? = null,
    val holders: List<AbilityHolder> = emptyList(),
    val tab: Tab = Tab.DETAILS,
    val error: AppError? = null,
) {
    /**
     * Nested rather than a type of its own, on the precedent of `DetailState.Tab`: a Contract holds
     * the Store's vocabulary, and which tab is open is a fact about this Store and nothing else. The
     * rendering half is `AbilityDetailTabUiModel`.
     */
    enum class Tab {
        DETAILS,
        HOLDERS,
    }
}
