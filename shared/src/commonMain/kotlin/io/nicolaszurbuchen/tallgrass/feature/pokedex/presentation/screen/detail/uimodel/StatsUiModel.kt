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
    val matchups: List<TypeMatchupUiModel>,
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

@Immutable
data class TypeMatchupUiModel(
    val typeLabel: String,
    val typeColor: Color,
    val factorText: String,
)
