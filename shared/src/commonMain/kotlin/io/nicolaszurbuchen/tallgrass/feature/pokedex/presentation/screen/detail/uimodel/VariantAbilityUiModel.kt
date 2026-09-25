package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One ability row in the Moves tab.
 *
 * [hiddenText] is set only for the third slot -- the one a Pokemon cannot ordinarily be caught with
 * -- and null is the common case rather than a missing value. A row without it draws no chip, which
 * is right here and not on the ability detail's grid: these are two or three rows in a column, so a
 * missing chip costs nothing to the alignment.
 */
@Immutable
data class VariantAbilityUiModel(
    val slug: String,
    val name: String,
    val initial: String,
    val shortEffect: String,
    val hiddenText: UiText?,
)
