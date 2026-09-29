package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * [totalFraction] is the lane beside [totalText], and is the mean of [bars]: a stat bar is full at
 * 160, so the total one is full at six times that. Any other scale would make the two incomparable
 * down the column they share.
 */
@Immutable
data class StatsUiModel(
    val bars: List<StatBarUiModel>,
    val totalText: String,
    val totalFraction: Float,
    /**
     * The six columns added up, which is arithmetic rather than a figure from the games.
     *
     * **No single Pokemon reaches either end.** A nature raises one stat and lowers another, and the
     * 510 EVs a Pokemon has to spend are a third of the 1,512 the six maxima assume — so the sum is
     * the bound on the column and not a total anything can have. It is here because a reader adding
     * the column up by hand gets the same number, and a table whose total row skips two of its five
     * columns reads as a table with something missing.
     */
    val totalMinText: String,
    val totalMaxText: String,
    // Only for the Gigantamax forms, which is where the question comes up. Dynamaxing multiplies the
    // HP a Pokemon already has rather than its base stat, so this is a note under the table rather
    // than a seventh row in it -- there is nothing to put in the other four columns.
    val dynamaxHpText: UiText?,
    // Two lists rather than one, because they answer different questions: what gets through, and
    // what bounces off. Either is empty for the Pokemon that has none of that kind -- Eelektross has
    // no weakness at all -- and the tab draws no heading over an empty one.
    val weaknesses: List<MatchupGroupUiModel>,
    val resistances: List<MatchupGroupUiModel>,
)

/**
 * [fraction] is the bar's length, already clamped — the component draws it and decides nothing.
 * [valueText] is the figure beside it, which is not the same number: a bar is full at 160 and the
 * value keeps going.
 *
 * [minText] and [maxText] are what the base stat becomes on a level 100 Pokemon at the two ends of
 * what a trainer can do to it. See `StatRange`, which is where the arithmetic and the reasoning are.
 */
@Immutable
data class StatBarUiModel(
    val label: UiText,
    val valueText: String,
    val fraction: Float,
    val minText: String,
    val maxText: String,
)

/**
 * The types that hit one defender by the same multiplier, under the multiplier as a title.
 *
 * The factor used to be on every chip, which meant "×2" printed four times in a row on most
 * Pokemon. As a left-hand title it is said once and the chips get their width back.
 */
@Immutable
data class MatchupGroupUiModel(
    val factorText: String,
    val types: List<TypeMatchupUiModel>,
)

@Immutable
data class TypeMatchupUiModel(
    val typeLabel: String,
    val typeColor: Color,
)
