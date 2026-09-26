package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityCellUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.CatchDifficultyUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * The Location tab: how this Pokemon can be got, which games have it, and where in the one chosen.
 *
 * The same grid as the location detail draws, pointed the other way round -- #24 found the two
 * screens were one component in two directions, and this is the second of them. It is **not scoped to
 * a region**: the question here is which of my games has this, and the answer crosses all of them.
 *
 * [catchRateText] and [catchDifficulty] are #43, moved off the About tab's Training block because
 * catch rate is a fact about meeting a Pokemon rather than about raising one. Both are shown: the
 * figure for anyone who knows the scale, and the word for everyone else.
 *
 * [emptyText] is two states rather than one, the same pair the location detail keeps apart. A game
 * with no rows says transfer it in; a Pokemon with no rows anywhere says it is never found in the
 * wild, which for Arceus and the starters is a fact and not a gap. See #21.
 */
@Immutable
data class LocationUiModel(
    val isLoading: Boolean,
    val captureMethods: List<CaptureMethodUiModel>,
    val catchRateText: UiText,
    val catchDifficulty: CatchDifficultyUiModel,
    val catchFraction: Float,
    val grid: AvailabilityGridUiModel,
    val selected: AvailabilityCellUiModel?,
    val breadcrumbText: UiText?,
    val places: List<VariantPlaceUiModel>,
    val emptyText: UiText?,
)
