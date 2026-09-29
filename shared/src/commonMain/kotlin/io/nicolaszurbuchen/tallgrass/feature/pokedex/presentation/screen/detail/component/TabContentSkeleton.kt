package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The silhouette of one tab's contents, for the two tabs that read for themselves.
 *
 * Moves and Location are not filled by the detail read: they go to the database when they are first
 * opened for a form, and again when a swipe puts a form under them that nobody has read. Without
 * something here the gap rendered as each tab's empty state -- "learns nothing" on one, a heading
 * over a grid with no games in it on the other -- which is a wrong answer rather than a missing one.
 *
 * No tab row, unlike [DetailSheetSkeleton]: by the time this draws the real one is already up, and
 * the reader is looking underneath it.
 */
@Composable
fun TabContentSkeleton(
    modifier: Modifier = Modifier,
    rows: Int = ROWS,
) {
    ShimmerPulse {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = modifier.fillMaxWidth(),
        ) {
            repeat(rows) {
                Box(modifier = Modifier.fillMaxWidth(ROW_WIDTH_FRACTION).height(ROW_HEIGHT).shimmerBlock())
            }
        }
    }
}

// The same figures as [DetailSheetSkeleton], so the two read as one silhouette when a reader sees
// both in a session.
private val ROW_HEIGHT = 14.dp
private const val ROW_WIDTH_FRACTION = 0.8f
private const val ROWS = 6
