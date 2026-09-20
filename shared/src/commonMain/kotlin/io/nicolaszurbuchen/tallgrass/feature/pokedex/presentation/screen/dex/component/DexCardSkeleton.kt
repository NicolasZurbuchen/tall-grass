package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
 * The card's own silhouette, drawn while the dex loads.
 *
 * The geometry is duplicated from [DexCard] on purpose. The point of a skeleton is that the real
 * content lands in a layout the eye has already settled on, so the two have to agree — and sharing a
 * layout between them would mean a component that takes a "is this real" flag and draws two things,
 * which is harder to keep honest than two files that look alike.
 */
@Composable
fun DexCardSkeleton(modifier: Modifier = Modifier) {
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
                        .size(56.dp)
                        .shimmerBlock(),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.sm),
            ) {
                Box(modifier = Modifier.fillMaxWidth(NAME_WIDTH_FRACTION).height(14.dp).shimmerBlock())
                Box(modifier = Modifier.size(width = 52.dp, height = 14.dp).shimmerBlock())
                Box(modifier = Modifier.size(width = 52.dp, height = 14.dp).shimmerBlock())
            }
        }
    }
}

private const val SKELETON_ASPECT_RATIO = 1.45f

// Enough of the card's width to read as a name without reaching the corner the artwork fills.
private const val NAME_WIDTH_FRACTION = 0.6f
