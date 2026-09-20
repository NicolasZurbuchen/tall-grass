package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.uimodel.PrefetchUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

@Immutable
data class DexUiModel(
    val isLoading: Boolean,
    val entries: List<DexEntryUiModel>,
    val error: AppErrorUiModel?,
    val prefetch: PrefetchUiModel?,
)

/**
 * [artworkKey] is the identity this card's artwork carries into the detail hero, and the name and
 * type keys are derived from it. They are on the card rather than built where they are drawn so that
 * both halves of the transition come from one place — see `SharedHero`.
 */
@Immutable
data class DexEntryUiModel(
    val slug: String,
    val numberText: String,
    val name: String,
    val formLabel: String?,
    val types: List<TypeUiModel>,
    val artworkUrl: String,
    val artworkKey: SharedElementKey,
    val tint: Color,
)
