package io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppPokeball
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

@Composable
fun HomeTile(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.White),
        modifier = modifier.aspectRatio(CARD_ASPECT_RATIO),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Bigger than the card is tall and pushed past its right edge, so all three of those
            // edges cut it. `requiredSize` rather than `size`, which would fit it to the card and
            // leave a whole ball sitting in the corner.
            AppPokeball(
                color = Color.White.copy(alpha = BALL_ALPHA),
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = BALL_CROP)
                        .requiredSize(BALL_SIZE),
            )

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        // Clear of the ball's solid middle, which is where a two-line label would
                        // otherwise run into it.
                        .padding(start = MaterialTheme.spacing.md, end = MaterialTheme.spacing.xxl),
            )
        }
    }
}

// Two columns of these at phone width is a card about seventy tall, which is the row the design
// draws: a label and a watermark, with no room for anything between them.
private const val CARD_ASPECT_RATIO = 2.4f

private val BALL_SIZE = 96.dp

// How far past the right edge the ball sits. Just over a third of it, which leaves the button and
// the band whole and takes the far side away.
private val BALL_CROP = 36.dp

// A watermark on a colour the label also sits on. Any heavier and the two compete.
private const val BALL_ALPHA = 0.16f
