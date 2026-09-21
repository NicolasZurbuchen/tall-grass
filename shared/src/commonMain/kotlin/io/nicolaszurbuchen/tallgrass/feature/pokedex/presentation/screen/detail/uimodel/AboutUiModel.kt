package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * The About tab.
 *
 * Only [heightText] and [weightText] change when the form does — everything below them is breeding
 * and training, which are true of the species. See #5 and #33.
 */
@Immutable
data class AboutUiModel(
    val heightText: UiText,
    val weightText: UiText,
    val gender: GenderUiModel,
    val eggGroupsText: UiText,
    val eggCycleText: UiText,
    val growthText: UiText,
)
