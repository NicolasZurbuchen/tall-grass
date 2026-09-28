package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionLocationUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.region_detail_location_games
import tallgrass.shared.generated.resources.region_detail_location_no_data
import tallgrass.shared.generated.resources.region_detail_location_one_game

/**
 * The sub-line is how many games have anything to meet here, and it says so in words when the answer
 * is none.
 *
 * "No encounter data" rather than "0 games", because a zero in that position reads as a count that
 * happens to be low rather than as the place having none at all -- which for Berry Forest in ten of
 * Kanto's twelve games is the answer a reader came for. See #24.
 *
 * One game gets its own string rather than "1 games".
 */
fun LocationSummary.toUiModel(): RegionLocationUiModel =
    RegionLocationUiModel(
        slug = slug,
        name = name,
        category = category.toUiModel(),
        gamesText =
            when (versionCount) {
                0 -> UiText.Resource(Res.string.region_detail_location_no_data)
                1 -> UiText.Resource(Res.string.region_detail_location_one_game)
                else -> UiText.Resource(Res.string.region_detail_location_games, listOf(versionCount))
            },
        hasEncounters = versionCount > 0,
    )
