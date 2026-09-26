package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_capture_gift
import tallgrass.shared.generated.resources.pokedex_detail_capture_trade
import tallgrass.shared.generated.resources.pokedex_detail_capture_transfer
import tallgrass.shared.generated.resources.pokedex_detail_capture_wild

/**
 * The coarse pills at the top of the Location tab.
 *
 * The rendering half of `CaptureMethod`, which is the domain's. #9 dropped the aggregate "methods
 * across all games" list in favour of these, because two of the answers are not encounter methods at
 * all: aggregating Pikachu's eight methods over 38 games destroyed the information, and no list of
 * methods can say "you cannot get this here".
 *
 * The colours are off the type palette on purpose. A pill here says how a Pokemon is got, and
 * borrowing a type colour would make it look like it was saying something about types.
 */
enum class CaptureMethodUiModel(
    val label: StringResource,
    val color: Color,
) {
    WILD_CATCH(Res.string.pokedex_detail_capture_wild, Color(0xFF3FAE72)),
    TRADE(Res.string.pokedex_detail_capture_trade, Color(0xFFC05C8E)),
    GIFT_OR_EVENT(Res.string.pokedex_detail_capture_gift, Color(0xFFC58B3C)),

    // Grey, because it is the absence of the others rather than a fifth way of getting one.
    TRANSFER_ONLY(Res.string.pokedex_detail_capture_transfer, Color(0xFF8FA0B5)),
}
