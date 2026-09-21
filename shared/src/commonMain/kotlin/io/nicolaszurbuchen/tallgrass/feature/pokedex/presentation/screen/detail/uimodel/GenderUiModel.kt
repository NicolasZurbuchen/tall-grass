package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * How a species' gender is drawn, which is two different rows rather than one row with two wordings.
 *
 * This was a single sentence -- "87.5% male, 12.5% female" -- and the sentence was hiding that
 * [Genderless] is not a share of anything. Splitting it here rather than in the tab is what lets the
 * About tab draw the symbols without asking whether there are any: a [Split] always has both.
 */
@Immutable
sealed interface GenderUiModel {
    /** Upstream writes -1 for Magnemite and the rest, which is a third case and not a rate of zero. */
    data object Genderless : GenderUiModel

    /**
     * The two shares, already formatted as percentages. Both are always present -- a species that is
     * all male is a hundred and a zero, and the zero is worth drawing.
     */
    data class Split(
        val maleText: UiText,
        val femaleText: UiText,
    ) : GenderUiModel
}
