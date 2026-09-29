package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One row of what a Gigantamax form has in front of it, which is not one of the moves it learnt.
 *
 * [sourcesText] is the moves that become it, and is the reason the row is legible: sixty moves come
 * out of the conversion as ten, and without it the list reads as a Pokemon that has forgotten most
 * of what it knew.
 *
 * [isSignature] marks the one row that is the form's own -- G-Max Wildfire against nine ordinary Max
 * Moves -- which is the whole of what having the Gigantamax factor changes.
 */
@Immutable
data class VariantMaxMoveUiModel(
    val slug: String,
    val name: String,
    val type: TypeUiModel,
    val damageClass: DamageClassUiModel,
    val powerText: String,
    val sourcesText: UiText,
    val isSignature: Boolean,
)
