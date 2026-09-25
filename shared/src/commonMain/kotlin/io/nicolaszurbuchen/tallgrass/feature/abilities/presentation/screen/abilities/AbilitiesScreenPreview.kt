package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@PreviewThemes
@Composable
private fun AbilitiesScreenPreview() {
    TallGrassPreview {
        AbilitiesScreen(
            state =
                AbilitiesUiModel(
                    isLoading = false,
                    abilities =
                        listOf(
                            AbilityUiModel(
                                slug = "adaptability",
                                name = "Adaptability",
                                initial = "A",
                                generationText = UiText.Raw("Gen 4"),
                                shortEffect = "Increases the same-type attack bonus from 1.5× to 2×.",
                            ),
                            AbilityUiModel(
                                slug = "blaze",
                                name = "Blaze",
                                initial = "B",
                                generationText = UiText.Raw("Gen 3"),
                                shortEffect = "Strengthens Fire moves to 1.5× their power when at 1/3 max HP or less.",
                            ),
                            // The case the two-line clamp exists for: a short effect long enough to
                            // wrap past where the card stops.
                            AbilityUiModel(
                                slug = "drizzle",
                                name = "Drizzle",
                                initial = "D",
                                generationText = UiText.Raw("Gen 3"),
                                shortEffect =
                                    "Summons rain that lasts for five turns when this Pokémon enters battle, " +
                                        "strengthening Water moves and weakening Fire moves for as long as it falls.",
                            ),
                            AbilityUiModel(
                                slug = "levitate",
                                name = "Levitate",
                                initial = "L",
                                generationText = UiText.Raw("Gen 3"),
                                shortEffect = "Evades Ground moves.",
                            ),
                            // The longest name in the dataset, which is what decides where the name
                            // has to stop rather than the generation beside it.
                            AbilityUiModel(
                                slug = "as-one-glastrier",
                                name = "As One (Glastrier)",
                                initial = "A",
                                generationText = UiText.Raw("Gen 8"),
                                shortEffect = "Combines Unnerve and Chilling Neigh.",
                            ),
                        ),
                    error = null,
                ),
            onAbilityClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
