package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail

sealed interface LocationDetailIntent {
    data class VersionSelected(
        val slug: String,
    ) : LocationDetailIntent

    /** Tapping the breadcrumb, which opens the grid again. */
    data object VersionCleared : LocationDetailIntent

    data class MethodSelected(
        val method: String,
    ) : LocationDetailIntent

    /**
     * One axis pinned, or unpinned when [value] is null.
     *
     * Null is "Any" and is a fourth option beside the states themselves rather than one of them: it
     * means unpinned, and shows the best case across every state.
     */
    data class ConditionSelected(
        val axis: String,
        val value: String?,
    ) : LocationDetailIntent

    data class PokemonClicked(
        val variantSlug: String,
    ) : LocationDetailIntent

    data object RetryClicked : LocationDetailIntent
}

sealed interface LocationDetailLabel {
    data class NavigateToPokemon(
        val cardSlug: String,
        val formSlug: String,
        val name: String,
        val artworkUrl: String,
        val primaryTypeSlug: String,
        val secondaryTypeSlug: String?,
    ) : LocationDetailLabel
}

sealed interface LocationDetailAction {
    data object LoadLocation : LocationDetailAction
}

sealed interface LocationDetailMessage {
    data object LoadStarted : LocationDetailMessage

    data class LocationLoaded(
        val location: LocationDetail,
        val conditions: List<EncounterCondition>,
    ) : LocationDetailMessage

    data class VersionSelected(
        val slug: String,
    ) : LocationDetailMessage

    data object VersionCleared : LocationDetailMessage

    data class EncountersLoaded(
        val encounters: List<Encounter>,
    ) : LocationDetailMessage

    data class MethodSelected(
        val method: String,
    ) : LocationDetailMessage

    data class ConditionSelected(
        val axis: String,
        val value: String?,
    ) : LocationDetailMessage

    data class LoadFailed(
        val error: AppError,
    ) : LocationDetailMessage
}

/**
 * [version] null is the grid open; non-null is the grid collapsed into a breadcrumb with the
 * encounters below it. That is the whole interaction #9 settled on, and the reason neither state
 * scrolls: the selector is never more than one tap away from what the reader is looking at.
 *
 * [encounters] is read per version rather than all at once. A busy route in a Generation VIII game is
 * hundreds of rows across a dozen versions, and all but one of them is behind a cell nobody tapped.
 *
 * [pinned] is the condition state, keyed by axis, and an axis missing from it is **unpinned** rather
 * than set to its default. Unpinned means "Any", which shows the best case across every state and is
 * why a rate under it reads "up to". Pin every axis a table varies on and the figures become exact.
 * See #24.
 *
 * [method] is the tab. Null until the encounters land, then the first method present -- a route has
 * no fixed set of methods, so there is nothing to default to before reading.
 */
data class LocationDetailState(
    val isLoading: Boolean = true,
    val location: LocationDetail? = null,
    val conditions: List<EncounterCondition> = emptyList(),
    val version: String? = null,
    val isLoadingEncounters: Boolean = false,
    val encounters: List<Encounter> = emptyList(),
    val method: String? = null,
    val pinned: Map<String, String> = emptyMap(),
    val error: AppError? = null,
)
