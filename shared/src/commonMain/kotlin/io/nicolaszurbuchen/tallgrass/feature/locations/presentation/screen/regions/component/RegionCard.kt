package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.RegionUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One region, drawn on its own colour with its box-art pair falling off the bottom-right corner.
 *
 * **The pair is the card's visual anchor**, which is #24's call: a single defining mascot was an
 * arbitrary editorial pick, and two removes the choice. They overlap deliberately, the second one
 * smaller and behind, so the card reads as a box rather than as two stickers.
 *
 * Hisui is the only region with one, and it is centred rather than left in half a pair's position --
 * Legends: Arceus shipped without a pair, and the layout says so instead of looking broken.
 *
 * The artwork is decorative: the region's name is beside it in words, so there is nothing for a
 * screen reader to find here that the card does not already say.
 */
@Composable
fun RegionCard(
    region: RegionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(CARD_RATIO)
                .clip(MaterialTheme.shapes.medium)
                .background(region.color ?: MaterialTheme.appColors.surfaceRaised)
                .clickable(onClick = onClick),
    ) {
        BoxArt(urls = region.boxArt, modifier = Modifier.align(Alignment.BottomEnd))

        Column(modifier = Modifier.padding(MaterialTheme.spacing.md)) {
            region.nativeName?.let { native ->
                // Absent for Orre, and the line simply goes: one card of eleven sitting a little
                // higher reads as a card, where a blank line reads as something that failed.
                Text(
                    text = native,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = NATIVE_ALPHA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = region.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = region.generationText.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = FACT_ALPHA),
                modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
            )

            Text(
                text = region.locationsText.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = FACT_ALPHA),
            )
        }
    }
}

@Composable
private fun BoxArt(
    urls: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.alpha(ART_ALPHA)) {
        // Drawn back to front, so the first of the pair finishes on top.
        urls.getOrNull(1)?.let { behind ->
            AsyncImage(
                model = behind,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(SECOND_ART).offset(x = SECOND_OFFSET, y = SECOND_OFFSET),
            )
        }

        AsyncImage(
            model = urls.firstOrNull(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .size(FIRST_ART)
                    .offset(
                        // A lone mascot sits square in the corner instead of leaving a gap where
                        // its partner would have been.
                        x = if (urls.size > 1) FIRST_OFFSET else SECOND_OFFSET,
                        y = FIRST_OFFSET,
                    ),
        )
    }
}

// Wider than tall, so eleven of them stack into a list that can be scanned rather than scrolled.
private const val CARD_RATIO = 1.55f

// The kana is a grace note rather than a label, so it sits well back from the name.
private const val NATIVE_ALPHA = 0.7f
private const val FACT_ALPHA = 0.85f

// Far enough back that white text stays readable over the artwork's lighter areas, near enough that
// the Pokemon is still recognisably itself.
private const val ART_ALPHA = 0.9f

private val FIRST_ART = 78.dp
private val SECOND_ART = 62.dp

// Both run off the corner: the card is a window onto the pair rather than a frame around it.
private val FIRST_OFFSET = 10.dp
private val SECOND_OFFSET = 22.dp
