package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@Immutable
data class RegionsUiModel(
    val isLoading: Boolean,
    val regions: List<RegionUiModel>,
    val error: AppErrorUiModel?,
)

/**
 * One card in the region list.
 *
 * [boxArt] is one or two artwork URLs and the card lays out differently for each: a pair overlaps,
 * a single one sits centred. Hisui is the only region with one, because Legends: Arceus shipped
 * without a pair on its cover.
 */
@Immutable
data class RegionUiModel(
    val slug: String,
    val name: String,
    val generationText: UiText,
    val locationsText: UiText,
    val color: Color?,
    val boxArt: List<String>,
)
