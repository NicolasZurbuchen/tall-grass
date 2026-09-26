package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The About tab's own silhouette, drawn while the three reads land.
 *
 * Only the first tab, because it is the only one visible: the pager has not been built yet, and a
 * skeleton for a tab nobody can see is work the reader never gets to notice.
 *
 * The geometry is duplicated from [RegionAboutTab] on purpose, on the same grounds as every other
 * skeleton here: the real content has to arrive into a layout the eye has already settled on.
 */
@Composable
fun RegionDetailSkeleton(modifier: Modifier = Modifier) {
    ShimmerPulse {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(TILES_HEIGHT).shimmerBlock())

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                modifier = Modifier.padding(top = MaterialTheme.spacing.md),
            ) {
                repeat(BLURB_LINES) { Box(modifier = Modifier.fillMaxWidth().height(LINE_HEIGHT).shimmerBlock()) }
                // Stopped short the way a paragraph ends rather than squared off.
                Box(modifier = Modifier.fillMaxWidth(LAST_LINE_FRACTION).height(LINE_HEIGHT).shimmerBlock())
            }

            repeat(DETAIL_ROWS) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                    modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.sm),
                ) {
                    Box(modifier = Modifier.fillMaxWidth(LABEL_FRACTION).height(LINE_HEIGHT).shimmerBlock())
                    Box(modifier = Modifier.fillMaxWidth().height(LINE_HEIGHT).shimmerBlock())
                }
            }
        }
    }
}

// The row of three figures, which is one block rather than three: it is drawn on a single raised
// surface, so its silhouette is that surface.
private val TILES_HEIGHT = 64.dp

private val LINE_HEIGHT = 14.dp
private const val BLURB_LINES = 3
private const val LAST_LINE_FRACTION = 0.6f

// Introduced, native name, games.
private const val DETAIL_ROWS = 3
private const val LABEL_FRACTION = 0.3f
