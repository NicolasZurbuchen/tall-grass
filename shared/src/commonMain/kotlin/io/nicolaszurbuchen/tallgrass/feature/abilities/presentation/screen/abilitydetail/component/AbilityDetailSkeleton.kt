package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.component

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
 * The sheet's silhouette while the ability is read.
 *
 * Only the sheet: the hero above it is the neutral surface the screen opens on rather than a guess.
 * An ability card hands nothing forward, because this transition is a push -- see
 * `AbilityDetailDestination`.
 */
@Composable
fun AbilityDetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
            Box(modifier = Modifier.size(width = TAB_WIDTH, height = TAB_HEIGHT).shimmerBlock())
            Box(modifier = Modifier.size(width = TAB_WIDTH, height = TAB_HEIGHT).shimmerBlock())
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

private val LINE_HEIGHT = 16.dp

// The short effect and the first lines of the paragraph under it. How much prose an ability has is
// one of the things being read, so this stands in for the shape rather than for the length.
private const val PROSE_LINES = 4
