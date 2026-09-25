package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityHolderUiModel
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@PreviewThemes
@Composable
private fun AbilityDetailScreenPreview() {
    TallGrassPreview {
        AbilityDetailScreen(
            state =
                AbilityDetailUiModel(
                    isLoading = false,
                    ability =
                        AbilityContentUiModel(
                            name = "Levitate",
                            generationText = UiText.Raw("Gen 3"),
                            shortEffect = "Evades Ground moves.",
                            effect =
                                "This Pokémon is immune to Ground-type moves, Spikes, Toxic Spikes and the " +
                                    "Arena Trap ability, and takes no damage from a sandstorm.",
                        ),
                    holders =
                        listOf(
                            AbilityHolderUiModel(
                                slug = "gastly",
                                name = "Gastly",
                                artworkUrl = "",
                                tint = TypeUiModel.GHOST,
                                hiddenText = null,
                            ),
                            // The case the third line exists for, and the reason it is reserved on
                            // the cards that have nothing to put in it.
                            AbilityHolderUiModel(
                                slug = "vibrava",
                                name = "Vibrava",
                                artworkUrl = "",
                                tint = TypeUiModel.GROUND,
                                hiddenText = UiText.Raw("Hidden"),
                            ),
                            AbilityHolderUiModel(
                                slug = "rotom",
                                name = "Rotom",
                                artworkUrl = "",
                                tint = TypeUiModel.ELECTRIC,
                                hiddenText = null,
                            ),
                        ),
                    tab = AbilityDetailTabUiModel.DETAILS,
                    error = null,
                ),
            onTabClick = { },
            onHolderClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
