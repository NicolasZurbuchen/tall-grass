package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * The About tab: three tiles, a paragraph, and a card of facts.
 *
 * [blurb] is original prose written for this project. A region's own in-game description would be
 * flavour text, which #10 and #11 forbid shipping.
 *
 * [pokedexText] reads as a dash rather than as 0 for Orre, which upstream has no regional dex for.
 * Zero in a row of figures reads as a figure, and this one is an absence.
 */
@Immutable
data class RegionAboutUiModel(
    val blurb: String,
    val pokedexText: UiText,
    val locationsText: UiText,
    val gamesText: UiText,
    val introducedText: UiText,
    val nativeName: String?,
    val gamesList: String,
)
