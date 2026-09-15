package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

sealed interface DexIntent {
    data class EntryClicked(
        val slug: String,
    ) : DexIntent

    data object RetryClicked : DexIntent
}

sealed interface DexLabel {
    data class NavigateToDetail(
        val slug: String,
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
