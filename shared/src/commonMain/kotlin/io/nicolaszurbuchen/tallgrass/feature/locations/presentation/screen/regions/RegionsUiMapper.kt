package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.mapper.toUiModel

/**
 * [regions] is mapped from the State by default, which is what every caller but one wants.
 *
 * `RegionsViewModel` passes its own, on the same grounds as `AbilitiesViewModel`: mapping the cards
 * is the expensive half of this function, even at eleven of them.
 */
fun RegionsState.toUiModel(regions: List<RegionUiModel> = this.regions.map { it.toUiModel() }): RegionsUiModel =
    RegionsUiModel(
        isLoading = isLoading,
        regions = regions,
        error = error?.toUiModel(),
    )
