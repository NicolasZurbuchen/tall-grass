package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityCellUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityRowUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.ConsoleUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.EncounterMethodUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.ConditionAxisUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.ConditionOptionUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.EncounterAreaUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.EncounterRowUiModel
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@PreviewThemes
@Composable
private fun LocationDetailScreenPreview() {
    TallGrassPreview {
        LocationDetailScreen(
            state =
                LocationDetailUiModel(
                    isLoading = false,
                    name = "Route 1",
                    regionName = "Kanto",
                    grid =
                        AvailabilityGridUiModel(
                            rows =
                                listOf(
                                    AvailabilityRowUiModel(
                                        console = ConsoleUiModel.DS,
                                        cells =
                                            listOf(
                                                AvailabilityCellUiModel("heartgold", "HeartGold", "HG", true),
                                                AvailabilityCellUiModel("soulsilver", "SoulSilver", "SS", true),
                                            ),
                                    ),
                                    AvailabilityRowUiModel(
                                        console = ConsoleUiModel.GB_GBC,
                                        cells =
                                            listOf(
                                                AvailabilityCellUiModel("red", "Red", "R", true),
                                                AvailabilityCellUiModel("blue", "Blue", "B", true),
                                                AvailabilityCellUiModel("gold", "Gold", "G", false),
                                            ),
                                    ),
                                ),
                        ),
                    // The collapsed state, which is the one worth looking at: the grid above is what
                    // it folds back into.
                    selected = AvailabilityCellUiModel("heartgold", "HeartGold", "HG", true),
                    breadcrumbText = UiText.Raw("9 species"),
                    isLoadingEncounters = false,
                    methods =
                        listOf(
                            EncounterMethodUiModel("walk", UiText.Raw("Tall grass")),
                            EncounterMethodUiModel("headbutt", UiText.Raw("Headbutt tree")),
                        ),
                    selectedMethod = "walk",
                    // HeartGold's Route 1, which is the table the correction on #8 was found on: it
                    // varies on three axes at once and reads as 360% until a state is chosen.
                    axes =
                        listOf(
                            ConditionAxisUiModel(
                                axis = "time",
                                label = UiText.Raw("Time"),
                                options =
                                    listOf(
                                        ConditionOptionUiModel("time-day", UiText.Raw("Day")),
                                        ConditionOptionUiModel("time-morning", UiText.Raw("Morning")),
                                        ConditionOptionUiModel("time-night", UiText.Raw("Night")),
                                    ),
                                selected = "time-day",
                            ),
                            ConditionAxisUiModel(
                                axis = "radio",
                                label = UiText.Raw("Radio"),
                                options =
                                    listOf(
                                        ConditionOptionUiModel("radio-off", UiText.Raw("Off")),
                                        ConditionOptionUiModel("radio-hoenn", UiText.Raw("Hoenn")),
                                    ),
                                selected = null,
                            ),
                        ),
                    areas =
                        listOf(
                            EncounterAreaUiModel(
                                slug = "kanto-route-1",
                                name = null,
                                rows =
                                    listOf(
                                        EncounterRowUiModel(
                                            variantSlug = "pidgey",
                                            name = "Pidgey",
                                            artworkUrl = "pidgey.png",
                                            primaryType = TypeUiModel.NORMAL,
                                            levelText = UiText.Raw("Lv 2-4"),
                                            rateText = UiText.Raw("up to 45%"),
                                            rateFraction = 0.45f,
                                            isBestCase = true,
                                        ),
                                        EncounterRowUiModel(
                                            variantSlug = "rattata",
                                            name = "Rattata",
                                            artworkUrl = "rattata.png",
                                            primaryType = TypeUiModel.NORMAL,
                                            levelText = UiText.Raw("Lv 2-4"),
                                            rateText = UiText.Raw("up to 30%"),
                                            rateFraction = 0.3f,
                                            isBestCase = true,
                                        ),
                                        // A method with no meaningful rate draws no figure and no
                                        // bar rather than a fabricated percentage.
                                        EncounterRowUiModel(
                                            variantSlug = "hoothoot",
                                            name = "Hoothoot",
                                            artworkUrl = "hoothoot.png",
                                            primaryType = TypeUiModel.FLYING,
                                            levelText = UiText.Raw("Lv 3"),
                                            rateText = null,
                                            rateFraction = 0f,
                                            isBestCase = true,
                                        ),
                                    ),
                            ),
                        ),
                    emptyText = null,
                    error = null,
                ),
            onVersionClick = { },
            onBreadcrumbClick = { },
            onMethodClick = { },
            onConditionClick = { _, _ -> },
            onPokemonClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
