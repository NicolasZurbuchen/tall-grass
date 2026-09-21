package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One move in a Pokemon's Moves tab.
 *
 * [howText] is the level where there is one and the method where there is not, which is the same slot
 * answering one question: how does this Pokemon come by the move. The mirror of the Learned by tab on
 * a move, which asks the same question of a Pokemon instead.
 */
@Immutable
data class VariantMoveUiModel(
    val slug: String,
    val name: String,
    val type: TypeUiModel,
    val damageClass: DamageClassUiModel,
    val powerText: String,
    val howText: UiText,
)
