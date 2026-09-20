package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexEntryUiModel

/**
 * One card. Separate from `DexUiMapper` so the dex can be mapped without mapping the state around
 * it — see `DexViewModel`, which holds onto the result across the states that do not change it.
 */
fun DexEntry.toUiModel(): DexEntryUiModel =
    DexEntryUiModel(
        slug = slug,
        numberText = "#" + dexNumber.toString().padStart(DEX_NUMBER_DIGITS, '0'),
        name = name,
        types = listOfNotNull(primaryType, secondaryType).map { it.toUiModel() },
        artworkUrl = artworkUrl,
        artworkKey = dexArtworkKey(slug),
        tint = primaryType.toUiModel().color,
    )

// Three, because the National Dex is four digits away from needing a fourth and every existing
// number reads as #001 rather than #1.
private const val DEX_NUMBER_DIGITS = 3
