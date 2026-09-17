package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

data class DexUiModel(
    val isLoading: Boolean,
    val entries: List<DexEntryUiModel>,
    val error: AppErrorUiModel?,
)

/**
 * [artworkKey] is the identity this card's artwork carries into the detail hero. It is on the card
 * rather than derived where it is drawn so that both halves of the transition come from one place.
 */
data class DexEntryUiModel(
    val slug: String,
    val numberText: String,
    val name: String,
    val formLabel: String?,
    val artworkUrl: String,
    val artworkKey: SharedElementKey,
    val tint: Color,
)
