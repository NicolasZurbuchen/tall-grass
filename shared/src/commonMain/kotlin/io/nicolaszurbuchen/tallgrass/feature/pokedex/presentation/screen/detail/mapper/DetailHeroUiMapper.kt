package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailHeroUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

/**
 * One card of the hero carousel.
 *
 * The defaults are what a card either side of the centre wants: its own artwork, its own colour, and
 * no shared-element key. The centred card overrides all three, because the form switcher may have
 * moved it off its species' default picture and because it is the only card that can be carrying the
 * key from the grid.
 */
fun DexEntry.toHeroUiModel(
    artworkUrl: String = this.artworkUrl,
    tint: Color = primaryType.toUiModel().color,
    artworkKey: SharedElementKey? = null,
): DetailHeroUiModel =
    DetailHeroUiModel(
        slug = slug,
        artworkUrl = artworkUrl,
        tint = tint,
        artworkKey = artworkKey,
    )
