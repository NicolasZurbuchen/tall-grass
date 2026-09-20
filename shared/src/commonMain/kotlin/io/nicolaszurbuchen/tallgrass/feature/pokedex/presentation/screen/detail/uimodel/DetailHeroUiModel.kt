package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

/**
 * One card in the hero carousel: the Pokemon on screen, and the ones either side of it.
 *
 * [artworkKey] is non-null on at most one of them — the card that was tapped, and only while the
 * form on screen is still the one that was tapped. Everything else in the carousel draws the same
 * picture and registers nothing, which is the same rule the dex grid follows. See
 * `DECISIONS.md § Only the tapped card is a shared element`.
 *
 * [tint] is the colour the screen takes when this card is centred.
 */
@Immutable
data class DetailHeroUiModel(
    val slug: String,
    val artworkUrl: String,
    val tint: Color,
    val artworkKey: SharedElementKey?,
)
