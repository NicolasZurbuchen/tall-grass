package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One ability, ready to draw: the sentence that says what it does, and the paragraph behind the
 * sentence.
 *
 * **Two paragraphs and a pill is all an ability is.** There is no trio of figures the way a move has
 * one -- generation, holder count and hidden-holder count were drawn here and removed, because
 * counting the grid on the next tab is not what someone opens an ability to find out. The tab is
 * emptier for it and says only true things.
 *
 * [effect] is null when upstream's long entry says exactly what [shortEffect] already said, which is
 * the case for 46 of the 314. The section is left out rather than filled, because a heading called
 * "In depth" over a repeat of the line above it is worse than no heading at all.
 */
@Immutable
data class AbilityContentUiModel(
    val name: String,
    val generationText: UiText,
    val shortEffect: String,
    val effect: String?,
)
