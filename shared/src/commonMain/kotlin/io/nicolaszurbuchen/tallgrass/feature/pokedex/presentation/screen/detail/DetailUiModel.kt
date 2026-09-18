package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

/**
 * [artworkUrl] and [tint] are never absent: both arrive with the destination, so the hero is drawn
 * complete on the first frame and only the sheet below it waits.
 *
 * [artworkKey] follows the form on screen rather than the one that was tapped. Switching form swaps
 * the artwork for another Pokemon, and a hero that kept the key would fly Charizard's card into a
 * picture of Mega Charizard X on the way back — so after a switch the key deliberately matches no
 * card and the two screens simply cross-fade.
 */
data class DetailUiModel(
    val isLoading: Boolean,
    val error: AppErrorUiModel?,
    val artworkUrl: String,
    val artworkKey: SharedElementKey,
    val tint: Color,
    val content: DetailContentUiModel?,
)
