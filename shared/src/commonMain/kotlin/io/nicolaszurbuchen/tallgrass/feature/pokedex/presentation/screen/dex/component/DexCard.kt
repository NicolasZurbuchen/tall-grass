package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypePill
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.nameKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.typeKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexEntryUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedBoundsOrNone
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedElementOrNone

/**
 * One Pokemon in the dex grid.
 *
 * [isHero] is true for the single card whose artwork, name and type pills are travelling into the
 * detail hero. A card that is not the hero draws exactly the same thing and registers nothing. See
 * `DECISIONS.md § Only the tapped card is a shared element`.
 */
@Composable
fun DexCard(
    entry: DexEntryUiModel,
    isHero: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // One key, or none. The name and the pills derive theirs from the artwork's so that the four
    // elements cannot disagree about which card is leaving -- see `SharedHero`.
    val heroKey = entry.artworkKey.takeIf { isHero }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = entry.tint, contentColor = Color.White),
        modifier = modifier.aspectRatio(CARD_ASPECT_RATIO),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = entry.artworkUrl,
                // The name is read out beside it, so describing the artwork too would have a screen
                // reader say every Pokemon twice.
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(ARTWORK_INSET)
                        .size(ARTWORK_SIZE)
                        .sharedElementOrNone(heroKey),
            )

            // Drawn after the artwork so the text stays legible where the two overlap, which at this
            // size they are meant to: the pills run under the Pokemon rather than stopping short.
            Column(modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.md)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier
                                .weight(1f, fill = false)
                                .sharedBoundsOrNone(heroKey?.nameKey()),
                    )
                    Text(
                        text = entry.numberText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = NUMBER_ALPHA),
                    )
                }

                // Stacked rather than in a row: at this width two pills side by side leave no room
                // for the artwork, which is the card's subject.
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                ) {
                    entry.types.forEachIndexed { slot, type ->
                        TypePill(
                            type = type,
                            modifier = Modifier.sharedBoundsOrNone(heroKey?.typeKey(slot)),
                        )
                    }
                }
            }
        }
    }
}

// Wider than tall, which is what two columns give a card that stacks a name and two pills down its
// left side while the artwork fills the corner.
private const val CARD_ASPECT_RATIO = 1.35f

private val ARTWORK_SIZE = 100.dp

// Off the corner rather than flush to it, so the Pokemon reads as standing on the card instead of
// being cropped by it.
private val ARTWORK_INSET = 4.dp

// The number is supporting text on a saturated ground, where full white reads as loud as the name
// beside it.
private const val NUMBER_ALPHA = 0.7f
