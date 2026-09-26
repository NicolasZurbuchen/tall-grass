package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The availability grid's own silhouette, drawn while the place loads.
 *
 * Only the grid, because it is the only thing on screen before a game is chosen: the tables below are
 * behind a cell nobody has tapped yet, and a skeleton for rows that need a second read would promise
 * something this state cannot deliver.
 */
@Composable
fun LocationDetailSkeleton(modifier: Modifier = Modifier) {
    ShimmerPulse {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            Box(modifier = Modifier.width(HEADING_WIDTH).height(HEADING_HEIGHT).shimmerBlock())

            repeat(ROWS) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
                ) {
                    Box(modifier = Modifier.width(ROW_LABEL_WIDTH).height(CELL_SIZE).shimmerBlock())
                    repeat(CELLS_PER_ROW) {
                        Box(modifier = Modifier.size(CELL_SIZE).shimmerBlock())
                    }
                }
            }
        }
    }
}

private val HEADING_WIDTH = 140.dp
private val HEADING_HEIGHT = 22.dp

// The same geometry the grid draws, so the cells land where the eye has already put them.
private val ROW_LABEL_WIDTH = 56.dp
private val CELL_SIZE = 30.dp

// Three rows of four is a Kanto-sized grid, which is the commonest shape.
private const val ROWS = 3
private const val CELLS_PER_ROW = 4
