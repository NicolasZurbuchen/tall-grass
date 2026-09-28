package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel

import org.jetbrains.compose.resources.StringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.region_detail_tab_about
import tallgrass.shared.generated.resources.region_detail_tab_locations
import tallgrass.shared.generated.resources.region_detail_tab_pokedex

/**
 * The rendering half of `RegionDetailState.Tab`, which is the Store's half.
 *
 * Two enums rather than one, on the precedent the Pokemon and ability details both set: a Contract
 * may not name a UiModel, and a label is a rendering concern the Store has no use for. The
 * declaration order is the order the tabs are drawn and the order the pager pages them.
 */
enum class RegionTabUiModel(
    val label: StringResource,
) {
    ABOUT(Res.string.region_detail_tab_about),
    LOCATIONS(Res.string.region_detail_tab_locations),
    POKEDEX(Res.string.region_detail_tab_pokedex),
}
