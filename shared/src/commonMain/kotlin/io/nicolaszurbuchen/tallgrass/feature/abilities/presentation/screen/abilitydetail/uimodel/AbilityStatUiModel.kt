package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/** One of the three figures at the top of the Details tab: a label and an already-formatted value. */
@Immutable
data class AbilityStatUiModel(
    val label: UiText,
    val valueText: String,
)
