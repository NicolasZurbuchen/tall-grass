package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey

fun DexState.toUiModel(): DexUiModel =
    DexUiModel(
        isLoading = isLoading,
        entries =
            entries.map { entry ->
                DexEntryUiModel(
                    slug = entry.slug,
                    numberText = "#" + entry.dexNumber.toString().padStart(DEX_NUMBER_DIGITS, '0'),
                    name = entry.name,
                    formLabel = entry.formLabel?.removeSuffix(" Form"),
                    artworkUrl = entry.artworkUrl,
                    artworkKey = dexArtworkKey(entry.slug),
                    tint = entry.primaryType.toUiModel().color,
                )
            },
        error = error?.toUiModel(),
    )

// Three, because the National Dex is four digits away from needing a fourth and every existing
// number reads as #001 rather than #1.
private const val DEX_NUMBER_DIGITS = 3
