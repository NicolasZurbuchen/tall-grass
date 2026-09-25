package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One Pokemon in the Known by tab.
 *
 * [hiddenText] is set only for the Pokemon that have this as their hidden ability, and null is the
 * ordinary case rather than a missing value -- most holders have it in a normal slot and there is
 * nothing to say about that. The card still reserves the line, so a grid of mixed rows keeps one
 * baseline.
 */
@Immutable
data class AbilityHolderUiModel(
    val slug: String,
    val name: String,
    val artworkUrl: String,
    val tint: TypeUiModel,
    val hiddenText: UiText?,
)
