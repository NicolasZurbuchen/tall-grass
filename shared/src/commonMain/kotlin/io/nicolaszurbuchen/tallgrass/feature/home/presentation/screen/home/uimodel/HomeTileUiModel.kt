package io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.uimodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Declaration order is the render order — the grid reads `entries` directly rather than keeping a
 * second list that could disagree with this one.
 */
enum class HomeTileUiModel(
    val label: StringResource,
    val icon: ImageVector,
) {
    POKEDEX(Res.string.home_tile_pokedex, Icons.Outlined.CatchingPokemon),
    MOVES(Res.string.home_tile_moves, Icons.Outlined.Bolt),
    ABILITIES(Res.string.home_tile_abilities, Icons.Outlined.AutoAwesome),
    ITEMS(Res.string.home_tile_items, Icons.Outlined.Inventory2),
    REGIONS(Res.string.home_tile_regions, Icons.Outlined.Map),
    TYPE_CHART(Res.string.home_tile_type_chart, Icons.Outlined.GridOn),
    TEAM_BUILDER(Res.string.home_tile_team_builder, Icons.Outlined.Groups),
    COMPARE(Res.string.home_tile_compare, Icons.AutoMirrored.Outlined.CompareArrows),
}
