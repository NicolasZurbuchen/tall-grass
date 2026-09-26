package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One card in a region's Pokedex tab.
 *
 * [numberText] is the **National** Dex number, the same as every other card in the app draws. The
 * regional entry number decides the order and is not shown: a reader who knows a Pokemon knows its
 * national number, and two numbers on one card would leave them working out which is which.
 *
 * [slug] is the region-native form and [cardSlug] is the card it opens through, which are not always
 * the same -- Alola's 37 is `vulpix-alola`, reached through `vulpix`. See #5.
 */
@Immutable
data class RegionDexCardUiModel(
    val slug: String,
    val cardSlug: String,
    val numberText: UiText,
    val name: String,
    val artworkUrl: String,
    val primaryType: TypeUiModel,
    val secondaryType: TypeUiModel?,
)
