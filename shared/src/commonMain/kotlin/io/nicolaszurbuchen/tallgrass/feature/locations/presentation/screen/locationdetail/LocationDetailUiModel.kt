package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityCellUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.EncounterMethodUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.ConditionAxisUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.EncounterAreaUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * [selected] null is the grid open; non-null is the grid collapsed into the breadcrumb with the
 * encounters underneath. Both states are drawn from the same [grid], which is why tapping the
 * breadcrumb can put it back without another read.
 *
 * [emptyText] is the line drawn in place of the tables, and it is **two states rather than one**: a
 * game upstream has no data for yet, and a game where nothing has been recorded here. #21 is explicit
 * that showing the first wording for the second case would be actively wrong.
 */
@Immutable
data class LocationDetailUiModel(
    val isLoading: Boolean,
    val name: String,
    val regionName: String,
    val grid: AvailabilityGridUiModel,
    val selected: AvailabilityCellUiModel?,
    val breadcrumbText: UiText?,
    val isLoadingEncounters: Boolean,
    val methods: List<EncounterMethodUiModel>,
    val selectedMethod: String?,
    val axes: List<ConditionAxisUiModel>,
    val areas: List<EncounterAreaUiModel>,
    val emptyText: UiText?,
    val error: AppErrorUiModel?,
)
