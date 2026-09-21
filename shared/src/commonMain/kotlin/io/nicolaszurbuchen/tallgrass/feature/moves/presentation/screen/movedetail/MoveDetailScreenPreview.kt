package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveBarUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveFactUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveLearnerUiModel
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@PreviewThemes
@Composable
private fun MoveDetailScreenPreview() {
    TallGrassPreview {
        MoveDetailScreen(
            state =
                MoveDetailUiModel(
                    isLoading = false,
                    move =
                        MoveContentUiModel(
                            name = "Flamethrower",
                            type = TypeUiModel.FIRE,
                            damageClass = DamageClassUiModel.SPECIAL,
                            ppText = UiText.Raw("15 PP"),
                            bars =
                                listOf(
                                    MoveBarUiModel(UiText.Raw("Power"), "90", 0.6f),
                                    MoveBarUiModel(UiText.Raw("Accuracy"), "100%", 1f),
                                    MoveBarUiModel(UiText.Raw("PP"), "15", 0.375f),
                                ),
                            effect = "Inflicts regular damage. Has a 10% chance to burn the target.",
                            targetText = UiText.Raw("One target"),
                            priorityText = "0",
                            // A chance that is stated and one that is not. Fire Punch burns 10% of
                            // the time; a move whose condition is certain says "always" rather than
                            // the 0% upstream stores.
                            facts =
                                listOf(
                                    MoveFactUiModel(UiText.Raw("Kind"), UiText.Raw("Damage and a condition")),
                                    MoveFactUiModel(UiText.Raw("Condition"), UiText.Raw("Burn · 10%")),
                                    MoveFactUiModel(UiText.Raw("Sp. Def"), UiText.Raw("-1 stage · always")),
                                ),
                        ),
                    // Both halves of the how column: a level where there is one, the method where
                    // there is not.
                    learners =
                        listOf(
                            MoveLearnerUiModel(
                                slug = "charizard",
                                name = "Charizard",
                                artworkUrl = "",
                                tint = TypeUiModel.FIRE,
                                howText = UiText.Raw("Lv 46"),
                            ),
                            MoveLearnerUiModel(
                                slug = "arcanine",
                                name = "Arcanine",
                                artworkUrl = "",
                                tint = TypeUiModel.FIRE,
                                howText = UiText.Raw("TM"),
                            ),
                            MoveLearnerUiModel(
                                slug = "vulpix-alola",
                                name = "Alolan Vulpix",
                                artworkUrl = "",
                                tint = TypeUiModel.ICE,
                                howText = UiText.Raw("Egg"),
                            ),
                        ),
                    tab = MoveDetailTabUiModel.DETAILS,
                    error = null,
                ),
            onTabClick = { },
            onLearnerClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
