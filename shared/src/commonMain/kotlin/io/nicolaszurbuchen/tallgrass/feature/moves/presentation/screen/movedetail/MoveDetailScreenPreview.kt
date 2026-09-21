package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.moveNameKey
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveFactUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveLearnerUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveStatUiModel
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
                            nameKey = moveNameKey("flamethrower"),
                            // The third of moves with no power draw a dash here rather than a zero.
                            stats =
                                listOf(
                                    MoveStatUiModel(UiText.Raw("Power"), "90"),
                                    MoveStatUiModel(UiText.Raw("Accuracy"), "100%"),
                                    MoveStatUiModel(UiText.Raw("PP"), "15"),
                                ),
                            effect = "Inflicts regular damage. Has a 10% chance to burn the target.",
                            // Target and priority lead because every move has them. After that, a
                            // chance that is stated and one that is not: Flamethrower burns 10% of
                            // the time, and a condition that is certain says "always" rather than
                            // the 0% upstream stores.
                            facts =
                                listOf(
                                    MoveFactUiModel(UiText.Raw("Target"), UiText.Raw("One target")),
                                    MoveFactUiModel(UiText.Raw("Priority"), UiText.Raw("0")),
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
