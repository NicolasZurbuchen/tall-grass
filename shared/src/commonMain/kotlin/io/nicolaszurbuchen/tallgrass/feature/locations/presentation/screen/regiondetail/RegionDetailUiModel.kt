package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionAboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionDexCardUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionLocationUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * [name] is empty until the read lands, and the header draws nothing rather than a placeholder:
 * nothing travels into this screen yet, because the shared-element transition #11 classifies for the
 * box-art pair is a later pass.
 *
 * [locations] is already filtered by the query. The unfiltered count rides along in [matchesText],
 * which is the line under the list -- a reader who has typed something needs to know how much of the
 * region they are no longer looking at.
 */
@Immutable
data class RegionDetailUiModel(
    val isLoading: Boolean,
    val name: String,
    val nativeName: String?,
    val subtitleText: UiText?,
    val color: Color?,
    val tab: RegionTabUiModel,
    val about: RegionAboutUiModel?,
    val query: String,
    val searchHint: UiText?,
    val locations: List<RegionLocationUiModel>,
    val matchesText: UiText?,
    val dex: List<RegionDexCardUiModel>,
    val error: AppErrorUiModel?,
)
