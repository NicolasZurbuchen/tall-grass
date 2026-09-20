package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.AboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.FormPillUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatBarUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.TypeMatchupUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@PreviewThemes
@Composable
private fun DetailScreenPreview() {
    TallGrassPreview {
        DetailScreen(
            state =
                DetailUiModel(
                    isLoading = false,
                    error = null,
                    name = "Charizard",
                    types = listOf(TypeUiModel.FIRE, TypeUiModel.FLYING),
                    artworkUrl = "",
                    artworkKey = SharedElementKey(source = "dex", id = "charizard"),
                    tint = TypeUiModel.FIRE.color,
                    content =
                        DetailContentUiModel(
                            numberText = "#006",
                            genusText = "Flame Pokémon",
                            forms =
                                listOf(
                                    FormPillUiModel("charizard", UiText.Raw("Standard")),
                                    FormPillUiModel("charizard-mega-x", UiText.Raw("Mega X")),
                                    FormPillUiModel("charizard-mega-y", UiText.Raw("Mega Y")),
                                    FormPillUiModel("charizard-gmax", UiText.Raw("Gigantamax")),
                                ),
                            activeFormSlug = "charizard",
                            tab = DetailTabUiModel.ABOUT,
                            about =
                                AboutUiModel(
                                    heightText = UiText.Raw("1.7 m"),
                                    weightText = UiText.Raw("90.5 kg"),
                                    genderText = UiText.Raw("87.5% male, 12.5% female"),
                                    eggGroupsText = UiText.Raw("Monster, Dragon"),
                                    eggCycleText = UiText.Raw("20 cycles"),
                                    growthText = UiText.Raw("Medium Slow"),
                                ),
                            stats =
                                StatsUiModel(
                                    bars =
                                        listOf(
                                            StatBarUiModel(UiText.Raw("HP"), 78, 0.49f),
                                            StatBarUiModel(UiText.Raw("Attack"), 84, 0.53f),
                                            StatBarUiModel(UiText.Raw("Defense"), 78, 0.49f),
                                            StatBarUiModel(UiText.Raw("Sp. Atk"), 109, 0.68f),
                                            StatBarUiModel(UiText.Raw("Sp. Def"), 85, 0.53f),
                                            StatBarUiModel(UiText.Raw("Speed"), 100, 0.63f),
                                        ),
                                    total = 534,
                                    matchups =
                                        listOf(
                                            TypeMatchupUiModel("Rock", TypeUiModel.ROCK.color, "×4"),
                                            TypeMatchupUiModel("Water", TypeUiModel.WATER.color, "×2"),
                                            TypeMatchupUiModel("Electric", TypeUiModel.ELECTRIC.color, "×2"),
                                            TypeMatchupUiModel("Ground", TypeUiModel.GROUND.color, "0"),
                                            TypeMatchupUiModel("Grass", TypeUiModel.GRASS.color, "¼"),
                                            TypeMatchupUiModel("Bug", TypeUiModel.BUG.color, "¼"),
                                        ),
                                ),
                        ),
                ),
            onBackClick = { },
            onFormClick = { },
            onTabClick = { },
            onRetryClick = { },
        )
    }
}
