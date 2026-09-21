package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The card's own silhouette, drawn while the grid loads.
 *
 * The geometry is duplicated from [MoveCard] on purpose, on the same grounds as `DexCardSkeleton`:
 * the real content has to land in a layout the eye has already settled on, and a component that takes
 * a "is this real" flag and draws two things is harder to keep honest than two files that look alike.
 */
@Composable
fun MoveCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.aspectRatio(SKELETON_ASPECT_RATIO),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(MaterialTheme.spacing.sm)
                        .size(GLYPH_BLOCK)
                        .shimmerBlock(),
            )

            Column(modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.md)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier =
                            Modifier
                                .weight(NAME_WIDTH_FRACTION)
                                .height(NAME_HEIGHT)
                                .shimmerBlock(),
                    )
                    Box(modifier = Modifier.weight(1f - NAME_WIDTH_FRACTION))
                }

                Box(
                    modifier =
                        Modifier
                            .padding(top = MaterialTheme.spacing.sm)
                            .size(width = PILL_WIDTH, height = PILL_HEIGHT)
                            .shimmerBlock(),
                )
            }
        }
    }
}

private const val SKELETON_ASPECT_RATIO = 1.35f

// A shade smaller than the glyph it stands in for: a solid block of that size reads as a filled
// corner rather than as something about to arrive.
private val GLYPH_BLOCK = 56.dp

private val NAME_HEIGHT = 18.dp
private val PILL_WIDTH = 56.dp
private val PILL_HEIGHT = 22.dp

// Enough of the card's width to read as a name without reaching the corner the figure sits in.
private const val NAME_WIDTH_FRACTION = 0.6f
