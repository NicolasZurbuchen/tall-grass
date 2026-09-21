package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * The screen's silhouette while the move is read.
 *
 * The hero is drawn in a neutral surface rather than in a type colour, because which colour it will
 * be is exactly what is not known yet: a move card hands nothing forward, so unlike the Pokemon
 * detail there is no tint to open with. Guessing one would mean a visible recolour a frame later.
 */
@Composable
fun MoveDetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(HERO_SHAPE)
                    .background(MaterialTheme.appColors.surfaceRaised),
        ) {
            // Measured by its content rather than given a height, so it lands where the real hero
            // does on every device: the status bar inset is part of that measurement.
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = MaterialTheme.spacing.md)
                        .padding(top = TOOLBAR_ROW_HEIGHT, bottom = MaterialTheme.spacing.lg),
            ) {
                Box(modifier = Modifier.fillMaxWidth(NAME_WIDTH_FRACTION).height(NAME_HEIGHT).shimmerBlock())

                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    Box(modifier = Modifier.size(width = PILL_WIDTH, height = PILL_HEIGHT).shimmerBlock())
                    Box(modifier = Modifier.size(PILL_HEIGHT).shimmerBlock())
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            repeat(BAR_ROWS) {
                Box(modifier = Modifier.fillMaxWidth().height(BAR_ROW_HEIGHT).shimmerBlock())
            }
        }
    }
}

private val HERO_SHAPE = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

private val TOOLBAR_ROW_HEIGHT = 56.dp
private val NAME_HEIGHT = 36.dp
private val PILL_WIDTH = 72.dp
private val PILL_HEIGHT = 28.dp
private val BAR_ROW_HEIGHT = 20.dp

// The three Battle Data lanes. Nothing stands in for the sections below them, because whether a move
// has an effect paragraph or any mechanics at all is one of the things being read.
private const val BAR_ROWS = 3

private const val NAME_WIDTH_FRACTION = 0.6f
