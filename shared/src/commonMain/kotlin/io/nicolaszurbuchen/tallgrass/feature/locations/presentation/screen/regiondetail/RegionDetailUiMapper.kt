package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toRegionThemeUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionAboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.region_detail_generation
import tallgrass.shared.generated.resources.regions_generation

fun RegionDetailState.toUiModel(): RegionDetailUiModel =
    RegionDetailUiModel(
        isLoading = isLoading,
        name = region?.name.orEmpty(),
        nativeName = region?.nativeName.orEmpty(),
        subtitleText = region?.let { UiText.Resource(Res.string.regions_generation, listOf(it.generation)) },
        color = region?.slug?.toRegionThemeUiModel()?.color,
        tab = RegionTabUiModel.entries[tab.ordinal],
        about =
            region?.let {
                RegionAboutUiModel(
                    blurb = it.blurb,
                    pokedexText = UiText.Raw(it.pokedexSize.toString()),
                    locationsText = UiText.Raw(it.locationCount.toString()),
                    gamesText = UiText.Raw(it.versions.size.toString()),
                    introducedText = UiText.Resource(Res.string.region_detail_generation, listOf(it.generation)),
                    nativeName = it.nativeName,
                    gamesList = it.versions.joinToString(GAME_SEPARATOR) { version -> version.name },
                )
            },
        locations = locations.map { it.toUiModel() },
        dex = dex.map { it.toUiModel() },
        error = error?.toUiModel(),
    )

private const val GAME_SEPARATOR = " · "
