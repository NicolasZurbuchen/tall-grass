package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel

import androidx.compose.runtime.Immutable

/**
 * One table of the chosen method, and the place it belongs to.
 *
 * **This is the unit that sums to 100%**, which is why the areas survive a fold the screen otherwise
 * performs. #24 folds a location's areas together and Canalave City reads as one place with several
 * rods; but an area is what a rate is a share *of*, and 14.5% of this dataset's groups draw one
 * method from more than one area -- up to 22. Merged, a bar reaches 2,200%.
 *
 * [name] is null when there is nothing to say: either the area is the place itself, or the location
 * has only this one and naming it would be labelling the only thing on screen. That null is what
 * makes the fold invisible in the 85.5% of cases where there is nothing to fold.
 */
@Immutable
data class EncounterAreaUiModel(
    val slug: String,
    val name: String?,
    val rows: List<EncounterRowUiModel>,
)
