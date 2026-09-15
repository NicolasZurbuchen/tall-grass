package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel

data class DexUiModel(
    val isLoading: Boolean,
    val entries: List<DexEntryUiModel>,
    val error: AppErrorUiModel?,
)

data class DexEntryUiModel(
    val slug: String,
    val numberText: String,
    val name: String,
    val formLabel: String?,
    val artworkUrl: String,
    val tint: Color,
)
