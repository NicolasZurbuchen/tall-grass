package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The card's own silhouette, drawn while the list loads.
 *
 * The geometry is duplicated from [AbilityCard] on purpose, on the same grounds as `MoveCardSkeleton`:
 * the real content has to land in a layout the eye has already settled on, and a component that takes
 * a "is this real" flag and draws two things is harder to keep honest than two files that look alike.
 */
@Composable
fun AbilityCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            Box(modifier = Modifier.size(TILE_SIZE).shimmerBlock())

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.fillMaxWidth(NAME_WIDTH_FRACTION).height(NAME_HEIGHT).shimmerBlock())
                Box(modifier = Modifier.fillMaxWidth().height(LINE_HEIGHT).shimmerBlock())
                Box(modifier = Modifier.fillMaxWidth(LAST_LINE_FRACTION).height(LINE_HEIGHT).shimmerBlock())
            }
        }
    }
}

private val TILE_SIZE = 44.dp
private val NAME_HEIGHT = 18.dp
private val LINE_HEIGHT = 13.dp

// Enough width to read as a name without reaching the generation that sits at the end of its row.
private const val NAME_WIDTH_FRACTION = 0.45f

// The effect's second line, stopped short the way a sentence ends rather than squared off.
private const val LAST_LINE_FRACTION = 0.7f
