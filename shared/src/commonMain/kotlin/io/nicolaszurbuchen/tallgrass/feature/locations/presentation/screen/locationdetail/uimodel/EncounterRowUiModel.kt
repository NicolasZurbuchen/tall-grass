package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One Pokemon on one route, in one game, under one method.
 *
 * [rateText] is null where the method has no meaningful rate at all -- raids, gifts, trades, SOS
 * calls -- and the row prints nothing rather than a fabricated percentage. #9 is explicit about that.
 *
 * [rateFraction] drives the bar behind the row and is the same number as [rateText] carries, as a
 * share of the table. It is **not** normalised to the largest row: the bar is a percentage of a
 * hundred, and stretching it to fill would tell the reader that the commonest Pokemon on a route is
 * a certainty.
 *
 * [isBestCase] is true while any axis is unpinned, and is what turns the rate into "up to X%". Pin
 * every axis the table varies on and the figures become exact and sum to 100.
 */
@Immutable
data class EncounterRowUiModel(
    val variantSlug: String,
    val name: String,
    val artworkUrl: String,
    val primaryType: TypeUiModel,
    val levelText: UiText,
    val rateText: UiText?,
    val rateFraction: Float,
    val isBestCase: Boolean,
)
