package io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.uimodel

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.home_tile_abilities
import tallgrass.shared.generated.resources.home_tile_compare
import tallgrass.shared.generated.resources.home_tile_items
import tallgrass.shared.generated.resources.home_tile_moves
import tallgrass.shared.generated.resources.home_tile_pokedex
import tallgrass.shared.generated.resources.home_tile_regions
import tallgrass.shared.generated.resources.home_tile_team_builder
import tallgrass.shared.generated.resources.home_tile_type_chart

/**
 * The eight destinations the home grid offers, in the order they appear.
 *
 * Declaration order is the render order -- the grid reads `entries` directly rather than keeping a
 * second list that could disagree with this one.
 *
 * The colour is carried here for the same reason `TypeUiModel` carries one: it is a fact about the
 * destination rather than about the card, so a second surface listing these tiles gets the same
 * eight colours without being told them again. It is not a theme token, and it does not change
 * between light and dark -- these are labels, the way a tube line is a colour.
 *
 * Six are read off the reference design. [TEAM_BUILDER] and [COMPARE] are not in it and are invented
 * into the two hues the other six leave open, magenta and green; everything nearer than that already
 * belongs to a tile above.
 */
enum class HomeTileUiModel(
    val label: StringResource,
    val color: Color,
) {
    POKEDEX(Res.string.home_tile_pokedex, Color(0xFF3EB8A0)),
    MOVES(Res.string.home_tile_moves, Color(0xFFE4666B)),
    ABILITIES(Res.string.home_tile_abilities, Color(0xFF4F9BE8)),
    ITEMS(Res.string.home_tile_items, Color(0xFFE9A83E)),
    REGIONS(Res.string.home_tile_regions, Color(0xFF7D5BC6)),
    TYPE_CHART(Res.string.home_tile_type_chart, Color(0xFFA28578)),
    TEAM_BUILDER(Res.string.home_tile_team_builder, Color(0xFFD45F9B)),
    COMPARE(Res.string.home_tile_compare, Color(0xFF5AA85C)),
}
