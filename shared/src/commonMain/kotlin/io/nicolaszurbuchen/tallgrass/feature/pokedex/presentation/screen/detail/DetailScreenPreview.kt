package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.AboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.CaptureMethodUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.CatchDifficultyUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailHeroUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.FormPillUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.GenderUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.LocationUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.MatchupGroupUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.MovesUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatBarUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.TypeMatchupUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantAbilityUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantMoveUiModel
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
                    numberText = "#006",
                    types = listOf(TypeUiModel.FIRE, TypeUiModel.FLYING),
                    tint = TypeUiModel.FIRE.color,
                    heroes =
                        listOf(
                            DetailHeroUiModel(
                                slug = "charmeleon",
                                name = "Charmeleon",
                                artworkUrl = "",
                                tint = TypeUiModel.FIRE.color,
                                artworkKey = null,
                            ),
                            DetailHeroUiModel(
                                slug = "charizard",
                                name = "Charizard",
                                artworkUrl = "",
                                tint = TypeUiModel.FIRE.color,
                                artworkKey = SharedElementKey(source = "dex", id = "charizard"),
                            ),
                            DetailHeroUiModel(
                                slug = "squirtle",
                                name = "Squirtle",
                                artworkUrl = "",
                                tint = TypeUiModel.WATER.color,
                                artworkKey = null,
                            ),
                        ),
                    activeIndex = 1,
                    content =
                        DetailContentUiModel(
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
                                    gender = GenderUiModel.Split(UiText.Raw("87.5%"), UiText.Raw("12.5%")),
                                    eggGroupsText = UiText.Raw("Monster, Dragon"),
                                    eggCycleText = UiText.Raw("20 cycles"),
                                    evYieldText = UiText.Raw("3 Sp. Atk"),
                                    baseExperienceText = UiText.Raw("267"),
                                    growthText = UiText.Raw("Medium Slow"),
                                ),
                            stats =
                                StatsUiModel(
                                    bars =
                                        listOf(
                                            // Charizard's real bands, so the preview shows the
                                            // widths the columns actually have to hold.
                                            StatBarUiModel(UiText.Raw("HP"), "78", 0.49f, "266", "360"),
                                            StatBarUiModel(UiText.Raw("Attack"), "84", 0.53f, "155", "293"),
                                            StatBarUiModel(UiText.Raw("Defense"), "78", 0.49f, "144", "280"),
                                            StatBarUiModel(UiText.Raw("Sp. Atk"), "109", 0.68f, "200", "348"),
                                            StatBarUiModel(UiText.Raw("Sp. Def"), "85", 0.53f, "157", "295"),
                                            StatBarUiModel(UiText.Raw("Speed"), "100", 0.63f, "184", "328"),
                                        ),
                                    totalText = "534",
                                    totalFraction = 0.56f,
                                    totalMinText = "1106",
                                    totalMaxText = "1904",
                                    // Charizard, which has one of each kind of row: a lone x4, a
                                    // pair at x2, an immunity, and a handful it barely feels.
                                    weaknesses =
                                        listOf(
                                            MatchupGroupUiModel(
                                                "×2",
                                                listOf(
                                                    TypeMatchupUiModel("Water", TypeUiModel.WATER.color),
                                                    TypeMatchupUiModel("Electric", TypeUiModel.ELECTRIC.color),
                                                ),
                                            ),
                                            MatchupGroupUiModel(
                                                "×4",
                                                listOf(TypeMatchupUiModel("Rock", TypeUiModel.ROCK.color)),
                                            ),
                                        ),
                                    resistances =
                                        listOf(
                                            MatchupGroupUiModel(
                                                "0",
                                                listOf(TypeMatchupUiModel("Ground", TypeUiModel.GROUND.color)),
                                            ),
                                            MatchupGroupUiModel(
                                                "¼",
                                                listOf(
                                                    TypeMatchupUiModel("Grass", TypeUiModel.GRASS.color),
                                                    TypeMatchupUiModel("Bug", TypeUiModel.BUG.color),
                                                ),
                                            ),
                                        ),
                                ),
                            moves =
                                MovesUiModel(
                                    isLoading = false,
                                    // A normal slot and the hidden one, which is what most Pokemon have.
                                    abilities =
                                        listOf(
                                            VariantAbilityUiModel(
                                                slug = "blaze",
                                                name = "Blaze",
                                                initial = "B",
                                                shortEffect =
                                                    "Strengthens Fire moves to 1.5× their power when at 1/3 max HP or less.",
                                                hiddenText = null,
                                            ),
                                            VariantAbilityUiModel(
                                                slug = "solar-power",
                                                name = "Solar Power",
                                                initial = "S",
                                                shortEffect =
                                                    "Boosts Special Attack in harsh sunlight, at the cost of HP each turn.",
                                                hiddenText = UiText.Raw("Hidden"),
                                            ),
                                        ),
                                    // Both halves of the how column: a level where there is one, the method
                                    // where there is not.
                                    learned =
                                        listOf(
                                            VariantMoveUiModel(
                                                slug = "flamethrower",
                                                name = "Flamethrower",
                                                type = TypeUiModel.FIRE,
                                                damageClass = DamageClassUiModel.SPECIAL,
                                                powerText = "90",
                                                howText = UiText.Raw("Lv 46"),
                                            ),
                                            VariantMoveUiModel(
                                                slug = "dragon-dance",
                                                name = "Dragon Dance",
                                                type = TypeUiModel.DRAGON,
                                                damageClass = DamageClassUiModel.STATUS,
                                                powerText = "—",
                                                howText = UiText.Raw("Egg"),
                                            ),
                                            VariantMoveUiModel(
                                                slug = "earthquake",
                                                name = "Earthquake",
                                                type = TypeUiModel.GROUND,
                                                damageClass = DamageClassUiModel.PHYSICAL,
                                                powerText = "100",
                                                howText = UiText.Raw("TM"),
                                            ),
                                        ),
                                    // Empty, and always is here: this preview is the ordinary
                                    // Charizard, and only a Gigantamax form has anything to convert.
                                    maxMoves = emptyList(),
                                ),
                            location =
                                LocationUiModel(
                                    isLoading = false,
                                    // Charizard: found in the wild in older games and nowhere in
                                    // the newest with data, which is what the second pill means.
                                    captureMethods =
                                        listOf(CaptureMethodUiModel.WILD_CATCH, CaptureMethodUiModel.TRANSFER_ONLY),
                                    catchRateText = UiText.Raw("45 / 255"),
                                    catchDifficulty = CatchDifficultyUiModel.HARD,
                                    catchFraction = 45f / 255f,
                                    grid = AvailabilityGridUiModel(emptyList()),
                                    selected = null,
                                    breadcrumbText = null,
                                    places = emptyList(),
                                    emptyText = null,
                                ),
                        ),
                ),
            onBackClick = { },
            onEntrySwipe = { },
            onFormClick = { },
            onTabClick = { },
            onAbilityClick = { },
            onMoveClick = { },
            onVersionClick = { },
            onBreadcrumbClick = { },
            onPlaceClick = { },
            onRetryClick = { },
        )
    }
}
