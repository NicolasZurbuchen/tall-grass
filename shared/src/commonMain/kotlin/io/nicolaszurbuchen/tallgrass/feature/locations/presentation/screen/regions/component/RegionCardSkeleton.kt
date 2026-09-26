package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The card's own silhouette, drawn while the eleven regions load.
 *
 * The geometry is duplicated from [RegionCard] on purpose, on the same grounds as the other
 * skeletons: the real content has to land in a layout the eye has already settled on.
 */
@Composable
fun RegionCardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(CARD_RATIO)
                .clip(MaterialTheme.shapes.medium)
                .shimmerBlock(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            modifier = Modifier.padding(MaterialTheme.spacing.md),
        ) {
            Box(modifier = Modifier.fillMaxWidth(NATIVE_WIDTH).height(NATIVE_HEIGHT).shimmerBlock())
            Box(modifier = Modifier.fillMaxWidth(NAME_WIDTH).height(NAME_HEIGHT).shimmerBlock())
        }
    }
}

private const val CARD_RATIO = 1.55f
private const val NATIVE_WIDTH = 0.3f
private const val NAME_WIDTH = 0.45f
private val NATIVE_HEIGHT = 12.dp
private val NAME_HEIGHT = 22.dp
