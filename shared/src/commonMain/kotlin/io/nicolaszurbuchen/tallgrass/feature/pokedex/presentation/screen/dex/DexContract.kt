package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

sealed interface DexIntent {
    data class EntryClicked(
        val slug: String,
    ) : DexIntent

    data object RetryClicked : DexIntent
}

/**
 * The card already knows the artwork and the colour the detail hero opens with, so the tap carries
 * them rather than leaving the next screen to look them up. See `HeroHandoff`.
 */
sealed interface DexLabel {
    data class NavigateToDetail(
        val slug: String,
        val artworkUrl: String,
        val primaryTypeSlug: String,
    ) : DexLabel
}

sealed interface DexAction {
    data object LoadEntries : DexAction
}

sealed interface DexMessage {
    data object LoadStarted : DexMessage

    data class EntriesLoaded(
        val entries: List<DexEntry>,
    ) : DexMessage

    data class LoadFailed(
        val error: AppError,
    ) : DexMessage
}

data class DexState(
    val isLoading: Boolean = true,
    val entries: List<DexEntry> = emptyList(),
    val error: AppError? = null,
)
