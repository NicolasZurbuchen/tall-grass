package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * [total] is a number rather than a string for the same reason [StatBarUiModel.value] is: it counts
 * to its new figure when the form changes, and a mapper cannot format a value that is still moving.
 */
@Immutable
data class StatsUiModel(
    val bars: List<StatBarUiModel>,
    val total: Int,
    val matchups: List<TypeMatchupUiModel>,
)

/**
 * [fraction] is the bar's length, already clamped — the component draws it and decides nothing.
 * [value] is the figure beside it, which is not the same number: a bar is full at 160 and the value
 * keeps going.
 */
@Immutable
data class StatBarUiModel(
    val label: UiText,
    val value: Int,
    val fraction: Float,
)

@Immutable
data class TypeMatchupUiModel(
    val typeLabel: String,
    val typeColor: Color,
    val factorText: String,
)
