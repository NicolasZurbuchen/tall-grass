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
 * One region, drawn on its own colour with its box-art pair in the bottom-right corner.
 *
 * **The pair is the card's visual anchor**, which is #24's call: a single defining mascot was an
 * arbitrary editorial pick, and two removes the choice. They overlap deliberately, the second smaller
 * and behind, so the card reads as a box rather than as two stickers.
 *
 * Hisui is the only region with one, and it is centred rather than left in half a pair's position --
 * Legends: Arceus shipped without a pair, and the layout says so instead of looking broken.
 *
 * **The artwork is inset rather than bled off the corner.** At two columns a card is half as wide as
 * it was, and mascots running past the edge read as clipped rather than as deliberate; kept inside,
 * the pair reads as the box art it is. The Japanese name went with the same change -- there is no room
 * for a third line of text beside artwork this size, and the About tab carries it.
 *
 * The artwork is decorative: the region's name is beside it in words, so there is nothing here for a
 * screen reader to find that the card does not already say.
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
        BoxArt(
            urls = region.boxArt,
            modifier = Modifier.align(Alignment.BottomEnd).padding(ART_INSET),
        )

        Column(modifier = Modifier.padding(MaterialTheme.spacing.md)) {
            Text(
                text = region.name,
                style = MaterialTheme.typography.titleLarge,
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
                modifier = Modifier.size(SECOND_ART).offset(x = PAIR_OFFSET),
            )
        }

        AsyncImage(
            model = urls.firstOrNull(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .size(FIRST_ART)
                    // A lone mascot sits square in the corner instead of leaving a gap where its
                    // partner would have been.
                    .offset(x = if (urls.size > 1) -PAIR_OFFSET else 0.dp),
        )
    }
}

// Close to square, because two columns of them is what the list is now: a wide card at half the width
// leaves the artwork bigger than the words.
private const val CARD_RATIO = 1.05f

private const val FACT_ALPHA = 0.85f

// Far enough back that white text stays readable over the artwork's lighter areas, near enough that
// the Pokemon is still recognisably itself.
private const val ART_ALPHA = 0.9f

private val FIRST_ART = 56.dp
private val SECOND_ART = 44.dp

// The pair leans apart by this much either side of centre, so both are visible without either running
// out of the card.
private val PAIR_OFFSET = 10.dp

// Keeps the pair off the card's edges. At two columns there is not enough width to bleed artwork past
// the corner without it reading as clipped.
private val ART_INSET = 6.dp
