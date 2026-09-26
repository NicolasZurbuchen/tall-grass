package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.LocationCategoryUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionAboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionDexCardUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionLocationUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@PreviewThemes
@Composable
private fun RegionDetailScreenPreview() {
    TallGrassPreview {
        RegionDetailScreen(
            state =
                RegionDetailUiModel(
                    isLoading = false,
                    name = "Kanto",
                    nativeName = "カントー",
                    subtitleText = UiText.Raw("Gen 1"),
                    color = Color(0xFFE8685D),
                    tab = RegionTabUiModel.LOCATIONS,
                    about =
                        RegionAboutUiModel(
                            blurb =
                                "The region the series began in: a peninsula of quiet towns and dense forest " +
                                    "strung along a coast, anchored by a volcanic island in the south.",
                            pokedexText = UiText.Raw("151"),
                            locationsText = UiText.Raw("96"),
                            gamesText = UiText.Raw("12"),
                            introducedText = UiText.Raw("Generation 1"),
                            nativeName = "カントー",
                            gamesList = "Red · Blue · Yellow · Gold · Silver",
                        ),
                    query = "",
                    searchHint = UiText.Raw("Search 96 locations"),
                    locations =
                        listOf(
                            RegionLocationUiModel(
                                slug = "kanto-route-1",
                                name = "Route 1",
                                category = LocationCategoryUiModel.ROUTE,
                                gamesText = UiText.Raw("12 games"),
                                hasEncounters = true,
                            ),
                            RegionLocationUiModel(
                                slug = "viridian-forest",
                                name = "Viridian Forest",
                                category = LocationCategoryUiModel.FOREST,
                                gamesText = UiText.Raw("8 games"),
                                hasEncounters = true,
                            ),
                            // #24's example, and the case the sub-line exists to say out loud: the
                            // place is listed although almost no game has anything in it.
                            RegionLocationUiModel(
                                slug = "berry-forest",
                                name = "Berry Forest",
                                category = LocationCategoryUiModel.FOREST,
                                gamesText = UiText.Raw("1 game"),
                                hasEncounters = true,
                            ),
                            RegionLocationUiModel(
                                slug = "kanto-victory-road-1",
                                name = "Victory Road",
                                category = LocationCategoryUiModel.OTHER,
                                gamesText = UiText.Raw("No encounter data"),
                                hasEncounters = false,
                            ),
                        ),
                    matchesText = null,
                    dex =
                        listOf(
                            RegionDexCardUiModel(
                                slug = "bulbasaur",
                                cardSlug = "bulbasaur",
                                numberText = UiText.Raw("#001"),
                                name = "Bulbasaur",
                                artworkUrl = "bulbasaur.png",
                                primaryType = TypeUiModel.GRASS,
                                secondaryType = TypeUiModel.POISON,
                            ),
                            RegionDexCardUiModel(
                                slug = "charmander",
                                cardSlug = "charmander",
                                numberText = UiText.Raw("#004"),
                                name = "Charmander",
                                artworkUrl = "charmander.png",
                                primaryType = TypeUiModel.FIRE,
                                secondaryType = null,
                            ),
                        ),
                    error = null,
                ),
            onTabClick = { },
            onQueryChange = { },
            onLocationClick = { },
            onPokemonClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
