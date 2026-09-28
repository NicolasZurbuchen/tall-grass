package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@PreviewThemes
@Composable
private fun RegionsScreenPreview() {
    TallGrassPreview {
        RegionsScreen(
            state =
                RegionsUiModel(
                    isLoading = false,
                    regions =
                        listOf(
                            RegionUiModel(
                                slug = "kanto",
                                name = "Kanto",
                                generationText = UiText.Raw("Gen 1"),
                                locationsText = UiText.Raw("96 locations"),
                                color = Color(0xFFE8685D),
                                boxArt = listOf("charizard.png", "blastoise.png"),
                            ),
                            RegionUiModel(
                                slug = "johto",
                                name = "Johto",
                                generationText = UiText.Raw("Gen 2"),
                                locationsText = UiText.Raw("67 locations"),
                                color = Color(0xFFF0C33C),
                                boxArt = listOf("ho-oh.png", "lugia.png"),
                            ),
                            // The only region with one mascot rather than a pair: Legends: Arceus
                            // shipped without one on its cover, and the artwork centres instead of
                            // leaving a gap where a partner would have been.
                            RegionUiModel(
                                slug = "hisui",
                                name = "Hisui",
                                generationText = UiText.Raw("Gen 8"),
                                locationsText = UiText.Raw("89 locations"),
                                color = Color(0xFF9DB945),
                                boxArt = listOf("arceus.png"),
                            ),
                            // The longest name in the set, which is what decides where a name has to
                            // stop at half width.
                            RegionUiModel(
                                slug = "paldea",
                                name = "Paldea",
                                generationText = UiText.Raw("Gen 9"),
                                locationsText = UiText.Raw("84 locations"),
                                color = Color(0xFFEE97CE),
                                boxArt = listOf("koraidon.png", "miraidon.png"),
                            ),
                            // An eleventh region this build has no colour for, drawn on the theme's
                            // own surface rather than dropped.
                            RegionUiModel(
                                slug = "somewhere-new",
                                name = "Somewhere New",
                                generationText = UiText.Raw("Gen 10"),
                                locationsText = UiText.Raw("1 location"),
                                color = null,
                                boxArt = emptyList(),
                            ),
                        ),
                    error = null,
                ),
            onRegionClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
