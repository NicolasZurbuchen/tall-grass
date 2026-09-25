package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityHolderUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One Pokemon in the Known by tab: a picture, a name, and whether the ability is its hidden one.
 *
 * Smaller than a dex card and carrying a third of what one carries, because the question here is
 * already answered -- the reader knows what the ability is and is asking who has it.
 *
 * **The third line is drawn whether or not it says anything.** Most holders have the ability in a
 * normal slot and there is no word worth putting there for them, but a grid whose cards are two lines
 * tall in some cells and three in others tears the rows apart. An empty line costs one row of pixels
 * and keeps every card the same height.
 */
@Composable
fun AbilityHolderCard(
    holder: AbilityHolderUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = holder.tint.color, contentColor = Color.White),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.sm),
        ) {
            Box(modifier = Modifier.size(ARTWORK_SIZE)) {
                AsyncImage(
                    model = holder.artworkUrl,
                    // The name is read out under it, so describing the artwork too would have a
                    // screen reader say every Pokemon twice.
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                text = holder.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(SLOT_LINE)) {
                holder.hiddenText?.let { hidden ->
                    Text(
                        text = hidden.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = HIDDEN_ALPHA),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private val ARTWORK_SIZE = 72.dp

// One line of bodySmall, reserved whether or not it is filled.
private val SLOT_LINE = 16.dp

// Supporting text under the name, on a saturated ground where full white reads as loud as the name
// above it.
private const val HIDDEN_ALPHA = 0.75f
