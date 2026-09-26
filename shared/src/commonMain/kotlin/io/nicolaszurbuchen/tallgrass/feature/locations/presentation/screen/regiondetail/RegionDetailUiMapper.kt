package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toRegionThemeUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionAboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.region_detail_generation
import tallgrass.shared.generated.resources.region_detail_search
import tallgrass.shared.generated.resources.region_detail_showing
import tallgrass.shared.generated.resources.regions_generation

/**
 * The filter is a plain case-insensitive `contains` over the name, with hyphens read as spaces so
 * that typing "route 3" finds `kanto-route-3`. Nothing smarter: the reader already knows the name of
 * the place they want, and a fuzzy match would put Route 13 above Route 3.
 */
fun RegionDetailState.toUiModel(): RegionDetailUiModel {
    val matching =
        if (query.isBlank()) {
            locations
        } else {
            val needle = query.trim().lowercase()
            locations.filter { it.name.lowercase().replace("-", " ").contains(needle) }
        }

    return RegionDetailUiModel(
        isLoading = isLoading,
        name = region?.name.orEmpty(),
        nativeName = region?.nativeName,
        subtitleText = region?.let { UiText.Resource(Res.string.regions_generation, listOf(it.generation)) },
        color = region?.slug?.toRegionThemeUiModel()?.color,
        tab = RegionTabUiModel.entries[tab.ordinal],
        about =
            region?.let {
                RegionAboutUiModel(
                    blurb = it.blurb,
                    // A dash rather than a 0 for Orre. A zero in a row of figures reads as a figure,
                    // and this one is upstream having no regional dex at all.
                    pokedexText = if (it.pokedexSize > 0) UiText.Raw(it.pokedexSize.toString()) else UiText.Raw(EM_DASH),
                    locationsText = UiText.Raw(it.locationCount.toString()),
                    gamesText = UiText.Raw(it.versions.size.toString()),
                    introducedText = UiText.Resource(Res.string.region_detail_generation, listOf(it.generation)),
                    nativeName = it.nativeName,
                    gamesList = it.versions.joinToString(GAME_SEPARATOR) { version -> version.name },
                )
            },
        query = query,
        searchHint = region?.let { UiText.Resource(Res.string.region_detail_search, listOf(it.locationCount)) },
        locations = matching.map { it.toUiModel() },
        // Only while something is typed. Unfiltered, the count is already on the About tab and on the
        // card that opened this screen, and a third copy under the list is furniture.
        matchesText =
            query.takeIf { it.isNotBlank() }
                ?.let { UiText.Resource(Res.string.region_detail_showing, listOf(matching.size, locations.size)) },
        // Empty for Orre, which upstream has no regional dex for. That is not an error and must not
        // borrow the error banner: the tab says so in its own words.
        dex = dex.map { it.toUiModel() },
        error = error?.toUiModel(),
    )
}

private const val EM_DASH = "—"

private const val GAME_SEPARATOR = " · "
