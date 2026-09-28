package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry

sealed interface RegionDetailIntent {
    data class TabSelected(
        val index: Int,
    ) : RegionDetailIntent

    data class QueryChanged(
        val query: String,
    ) : RegionDetailIntent

    data class LocationClicked(
        val slug: String,
    ) : RegionDetailIntent

    data class PokemonClicked(
        val slug: String,
    ) : RegionDetailIntent

    data object RetryClicked : RegionDetailIntent
}

sealed interface RegionDetailLabel {
    data class NavigateToLocation(
        val slug: String,
    ) : RegionDetailLabel

    /**
     * The six fields a Pokemon detail needs to draw its hero before it has read anything.
     *
     * Taken apart rather than carried as the Pokedex feature's own handoff type, because a feature
     * may only import from its own subtree. `app/` is where the two halves meet.
     */
    data class NavigateToPokemon(
        val cardSlug: String,
        val formSlug: String,
        val name: String,
        val artworkUrl: String,
        val primaryTypeSlug: String,
        val secondaryTypeSlug: String?,
    ) : RegionDetailLabel
}

sealed interface RegionDetailAction {
    data object LoadRegion : RegionDetailAction
}

sealed interface RegionDetailMessage {
    data object LoadStarted : RegionDetailMessage

    data class RegionLoaded(
        val region: RegionDetail,
        val locations: List<LocationSummary>,
        val dex: List<RegionDexEntry>,
    ) : RegionDetailMessage

    data class TabChanged(
        val tab: RegionDetailState.Tab,
    ) : RegionDetailMessage

    data class QueryChanged(
        val query: String,
    ) : RegionDetailMessage

    data class LoadFailed(
        val error: AppError,
    ) : RegionDetailMessage
}

/**
 * [region], [locations] and [dex] all arrive together rather than tab by tab.
 *
 * Three reads of one local database is one round trip, and a tab that populates a beat after it is
 * tapped reads as slower than one that was always ready -- the same call the ability detail made for
 * its holders. The largest of the three is Kalos at 457 dex rows, which is well inside what the dex
 * grid already reads in one go.
 *
 * [query] filters the Locations tab and nothing else. It lives in the Store rather than in the
 * composable because the tabs are a pager: swiping away and back would otherwise clear what was
 * typed.
 */
data class RegionDetailState(
    val isLoading: Boolean = true,
    val region: RegionDetail? = null,
    val locations: List<LocationSummary> = emptyList(),
    val dex: List<RegionDexEntry> = emptyList(),
    val query: String = "",
    val tab: Tab = Tab.ABOUT,
    val error: AppError? = null,
) {
    /**
     * Nested rather than a type of its own, on the precedent of `DetailState.Tab`: a Contract holds
     * the Store's vocabulary, and which tab is open is a fact about this Store and nothing else. The
     * rendering half is `RegionTabUiModel`.
     */
    enum class Tab {
        ABOUT,
        LOCATIONS,
        POKEDEX,
    }
}
