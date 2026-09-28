package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toRegionThemeUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.RegionUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.regions_generation
import tallgrass.shared.generated.resources.regions_locations

/**
 * A region this build has no colour for keeps a null and the card draws it on the theme's own
 * surface, rather than being dropped. Unlike a dex card with no type, there is still a whole region
 * to draw -- a name, a dex and a hundred places -- and losing all of that over a missing swatch would
 * be the wrong trade.
 *
 * Null rather than a constant here, because the fallback is a theme colour and this is not a
 * composable: inventing a fixed grey would be the one colour in the app that does not follow the
 * theme.
 */
fun Region.toUiModel(): RegionUiModel =
    RegionUiModel(
        slug = slug,
        name = name,
        nativeName = nativeName,
        generationText = UiText.Resource(Res.string.regions_generation, listOf(generation)),
        locationsText = UiText.Resource(Res.string.regions_locations, listOf(locationCount)),
        color = slug.toRegionThemeUiModel()?.color,
        boxArt = boxArt,
    )
