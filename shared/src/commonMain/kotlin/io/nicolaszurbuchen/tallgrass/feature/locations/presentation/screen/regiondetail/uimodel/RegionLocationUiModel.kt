package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.LocationCategoryUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One row in a region's Locations tab.
 *
 * [hasEncounters] is false for the places no game has anything at, and those rows stay in the list
 * and stay tappable. Berry Forest exists in every Kanto game and carries encounters in two of them,
 * and that absence is what a reader came to find out -- see #24. What changes is the sub-line, which
 * says so in words rather than showing a zero.
 */
@Immutable
data class RegionLocationUiModel(
    val slug: String,
    val name: String,
    val category: LocationCategoryUiModel,
    val gamesText: UiText,
    val hasEncounters: Boolean,
)
