package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The sheet's silhouette while the read lands: a tab row and a handful of rows under it.
 *
 * The geometry echoes the real tabs on purpose — the point of a skeleton is that the content arrives
 * into a layout the eye has already settled on, which only works if the two agree.
 */
@Composable
fun DetailSheetSkeleton(modifier: Modifier = Modifier) {
    ShimmerPulse {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
                Box(modifier = Modifier.width(TAB_WIDTH).height(TAB_HEIGHT).shimmerBlock())
                Box(modifier = Modifier.width(TAB_WIDTH).height(TAB_HEIGHT).shimmerBlock())
            }

            repeat(ROWS) {
                Box(modifier = Modifier.fillMaxWidth(ROW_WIDTH_FRACTION).height(ROW_HEIGHT).shimmerBlock())
            }
        }
    }
}

private val TAB_WIDTH = 64.dp
private val TAB_HEIGHT = 16.dp
private val ROW_HEIGHT = 14.dp
private const val ROW_WIDTH_FRACTION = 0.8f
private const val ROWS = 6
