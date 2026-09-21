package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes

@PreviewThemes
@Composable
private fun MovesScreenPreview() {
    TallGrassPreview {
        MovesScreen(
            state =
                MovesUiModel(
                    isLoading = false,
                    moves =
                        listOf(
                            MoveUiModel(
                                slug = "flamethrower",
                                name = "Flamethrower",
                                type = TypeUiModel.FIRE,
                                damageClass = DamageClassUiModel.SPECIAL,
                                powerText = "90",
                            ),
                            MoveUiModel(
                                slug = "close-combat",
                                name = "Close Combat",
                                type = TypeUiModel.FIGHTING,
                                damageClass = DamageClassUiModel.PHYSICAL,
                                powerText = "120",
                            ),
                            // The case the em dash exists for: a third of the list has no power, and
                            // a status move is not a move that hits for zero.
                            MoveUiModel(
                                slug = "thunder-wave",
                                name = "Thunder Wave",
                                type = TypeUiModel.ELECTRIC,
                                damageClass = DamageClassUiModel.STATUS,
                                powerText = "—",
                            ),
                            MoveUiModel(
                                slug = "dragon-dance",
                                name = "Dragon Dance",
                                type = TypeUiModel.DRAGON,
                                damageClass = DamageClassUiModel.STATUS,
                                powerText = "—",
                            ),
                            // The longest name in the dataset, which is what decides where the name
                            // has to stop rather than the power figure.
                            MoveUiModel(
                                slug = "10-000-000-volt-thunderbolt",
                                name = "10,000,000 Volt Thunderbolt",
                                type = TypeUiModel.ELECTRIC,
                                damageClass = DamageClassUiModel.SPECIAL,
                                powerText = "195",
                            ),
                            MoveUiModel(
                                slug = "moonblast",
                                name = "Moonblast",
                                type = TypeUiModel.FAIRY,
                                damageClass = DamageClassUiModel.SPECIAL,
                                powerText = "95",
                            ),
                        ),
                    error = null,
                ),
            onMoveClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
