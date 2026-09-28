package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One control in the condition selector: a question, its answers, and which one is pinned.
 *
 * **Only the axes the table in front of the reader actually varies on.** FireRed's Route 1 shows
 * none and HeartGold's shows three -- time, swarm and radio -- and that is data rather than a layout
 * anyone wrote down. A control for an axis with one possible value would be asking a question with
 * one answer.
 *
 * [selected] null is "Any", which is unpinned rather than a value: it shows the best case across
 * every state on this axis, and is what makes a rate read "up to". See #24.
 */
@Immutable
data class ConditionAxisUiModel(
    val axis: String,
    val label: UiText,
    val options: List<ConditionOptionUiModel>,
    val selected: String?,
)

@Immutable
data class ConditionOptionUiModel(
    val slug: String,
    val label: UiText,
)
