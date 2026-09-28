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
 * [locations] is every place in the region. There is no search: the first attempt lost focus on every
 * keystroke, because the field sits inside a pager page that recomposes whole, and a broken field is
 * worse than a long list. See `DECISIONS.md`.
 */
@Immutable
data class RegionDetailUiModel(
    val isLoading: Boolean,
    val name: String,
    val nativeName: String,
    val subtitleText: UiText?,
    val color: Color?,
    val tab: RegionTabUiModel,
    val about: RegionAboutUiModel?,
    val locations: List<RegionLocationUiModel>,
    val dex: List<RegionDexCardUiModel>,
    val error: AppErrorUiModel?,
)
