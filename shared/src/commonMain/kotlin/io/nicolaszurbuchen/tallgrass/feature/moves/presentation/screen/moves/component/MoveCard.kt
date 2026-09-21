package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
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
import io.nicolaszurbuchen.tallgrass.core.move.presentation.component.DamageClassIcon
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypePill
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.MoveUiModel
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.moves_power

/**
 * One move in the grid.
 *
 * The dex card's layout with a move's contents in it: the type's colour as the ground, the name and a
 * pill down the left, a figure in the top corner, and the subject filling the bottom-right corner
 * cropped by the card's own edges.
 *
 * **The subject is the damage class.** Where a Pokemon has artwork, a move has one of three symbols,
 * and putting it where the artwork goes is what makes the two grids read as the same grid. It is also
 * the only thing on the card that says how the move deals its damage — the power figure says how
 * much, and a 90-power Flamethrower and a 90-power Close Combat are answered by different defences.
 */
@Composable
fun MoveCard(
    move: MoveUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = move.type.color, contentColor = Color.White),
        modifier = modifier.aspectRatio(CARD_ASPECT_RATIO),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Pushed past the corner it stands in so the card's right and bottom edges take the far
            // side of it, exactly as a dex card crops its watermark. Sized by width alone: the
            // vectors are 3:2 and fixing both dimensions would squash them.
            DamageClassIcon(
                damageClass = move.damageClass,
                color = Color.White.copy(alpha = GLYPH_ALPHA),
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = GLYPH_CROP, y = GLYPH_CROP)
                        .requiredWidth(GLYPH_WIDTH),
            )

            Column(modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.md)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = move.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    // The corner a dex card puts its number in. Labelled, because "90" alone is not
                    // self-evident the way "#001" is.
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(start = MaterialTheme.spacing.sm),
                    ) {
                        Text(text = move.powerText, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(Res.string.moves_power),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = LABEL_ALPHA),
                        )
                    }
                }

                TypePill(type = move.type, modifier = Modifier.padding(top = MaterialTheme.spacing.sm))
            }
        }
    }
}

// The dex card's, so the two grids scroll at the same rhythm.
private const val CARD_ASPECT_RATIO = 1.35f

// Wider than the card's own half, so the glyph reads as something the card is cut out of rather than
// as a symbol placed on it.
private val GLYPH_WIDTH = 150.dp

// How far past the corner it sits, the same in both directions -- the two edges crop it together,
// which is what makes it look like one corner rather than two cuts.
private val GLYPH_CROP = GLYPH_WIDTH * 0.12f

// Stronger than the pokeball watermark it replaced. This one is the card's subject rather than its
// brand, so it is meant to be read rather than merely sensed.
private const val GLYPH_ALPHA = 0.3f

// The word under the figure is supporting text on a saturated ground, where full white reads as loud
// as the number it labels.
private const val LABEL_ALPHA = 0.7f
