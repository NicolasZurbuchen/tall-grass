package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One ability, ready to draw, in the order it is read: the three figures that place it, the sentence
 * that says what it does, and the paragraph behind the sentence.
 *
 * [effect] is null when upstream's long entry says exactly what [shortEffect] already said, which is
 * the case for 46 of the 314. The section is left out rather than filled, because a heading called
 * "In depth" over a repeat of the line above it is worse than no heading at all.
 */
@Immutable
data class AbilityContentUiModel(
    val name: String,
    val generationText: UiText,
    val stats: List<AbilityStatUiModel>,
    val shortEffect: String,
    val effect: String?,
)
