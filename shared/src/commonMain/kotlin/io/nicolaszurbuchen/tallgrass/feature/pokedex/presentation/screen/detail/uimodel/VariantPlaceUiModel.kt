package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.EncounterMethodUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One place this Pokemon turns up in the chosen game.
 *
 * **No method tabs and no condition selector here**, which is #24's split: one Pokemon in one game
 * yields one to three rows, so tabs would be chrome over nothing, where one route in one game yields
 * up to five tables. The method rides on the row instead, as the actual method rather than a group.
 *
 * [rateText] is null where the method has no meaningful rate, and is qualified with "up to" wherever
 * the row is one of several condition states -- the same convention the route side uses, for the same
 * reason. Nothing on this side pins a state, so a conditioned row is always a best case.
 */
@Immutable
data class VariantPlaceUiModel(
    val locationSlug: String,
    val locationName: String,
    val areaName: String?,
    val method: EncounterMethodUiModel,
    val levelText: UiText,
    val rateText: UiText?,
    val conditionText: UiText?,
)
