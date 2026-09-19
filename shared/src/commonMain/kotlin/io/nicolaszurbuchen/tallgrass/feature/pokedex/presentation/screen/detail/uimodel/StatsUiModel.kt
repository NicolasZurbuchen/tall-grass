package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@Immutable
data class StatsUiModel(
    val bars: List<StatBarUiModel>,
    val totalText: String,
    val matchups: List<TypeMatchupUiModel>,
)

/** [fraction] is the bar's length, already clamped — the component draws it and decides nothing. */
@Immutable
data class StatBarUiModel(
    val label: UiText,
    val valueText: String,
    val fraction: Float,
)

@Immutable
data class TypeMatchupUiModel(
    val typeLabel: String,
    val typeColor: Color,
    val factorText: String,
)
