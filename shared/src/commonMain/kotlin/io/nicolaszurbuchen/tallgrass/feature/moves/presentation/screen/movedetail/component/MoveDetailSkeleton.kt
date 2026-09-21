package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The sheet's silhouette while the move is read.
 *
 * Only the sheet: the hero above it has no colour to draw yet either, and it is left as the neutral
 * surface the screen opens on rather than guessed at. A move card hands nothing forward — not even
 * the type — so unlike a Pokemon's detail there is nothing to render on the first frame. See #11 on
 * why this transition is a push.
 */
@Composable
fun MoveDetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
            Box(modifier = Modifier.size(width = TAB_WIDTH, height = TAB_HEIGHT).shimmerBlock())
            Box(modifier = Modifier.size(width = TAB_WIDTH, height = TAB_HEIGHT).shimmerBlock())
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.lg),
        ) {
            repeat(STAT_TILES) {
                Box(modifier = Modifier.weight(1f).height(TILE_HEIGHT).shimmerBlock())
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.lg),
        ) {
            repeat(PROSE_LINES) {
                Box(modifier = Modifier.fillMaxWidth().height(LINE_HEIGHT).shimmerBlock())
            }
        }
    }
}

private val TAB_WIDTH = 72.dp
private val TAB_HEIGHT = 20.dp

// What the three figures measure: a label, a value and the tile's padding.
private val TILE_HEIGHT = 64.dp
private val LINE_HEIGHT = 16.dp

private const val STAT_TILES = 3

// The effect paragraph, which is two or three lines for almost every move that has one. Nothing
// stands in for the mechanics below it, because how many rows a move has is one of the things being
// read.
private const val PROSE_LINES = 3
