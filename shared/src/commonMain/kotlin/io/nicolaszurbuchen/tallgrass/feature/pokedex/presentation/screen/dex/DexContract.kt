package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.infra.image.ImagePrefetchProgress

sealed interface DexIntent {
    data class EntryClicked(
        val slug: String,
    ) : DexIntent

    data object RetryClicked : DexIntent
}

/**
 * The card already knows everything the detail hero opens with, so the tap carries it rather than
 * leaving the next screen to look it up. See `HeroHandoff`.
 */
sealed interface DexLabel {
    data class NavigateToDetail(
        val slug: String,
        val hero: HeroHandoff,
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

    data class ArtworkPrefetchProgressed(
        val progress: ImagePrefetchProgress,
    ) : DexMessage
}

/**
 * [prefetch] is null until the artwork run starts and stays put once it finishes, so the banner can
 * say what happened rather than vanishing the moment the last image lands.
 */
data class DexState(
    val isLoading: Boolean = true,
    val entries: List<DexEntry> = emptyList(),
    val error: AppError? = null,
    val prefetch: ImagePrefetchProgress? = null,
)
