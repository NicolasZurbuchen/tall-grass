package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveLearnerUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One Pokemon in the Learned by tab: a picture, a name, and how it comes by the move.
 *
 * Smaller than a dex card and carrying a third of what one carries, because the question here is
 * already answered — the reader knows what the move is and is asking who has it.
 */
@Composable
fun MoveLearnerCard(
    learner: MoveLearnerUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = learner.tint.color, contentColor = Color.White),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.sm),
        ) {
            Box(modifier = Modifier.size(ARTWORK_SIZE)) {
                AsyncImage(
                    model = learner.artworkUrl,
                    // The name is read out under it, so describing the artwork too would have a
                    // screen reader say every Pokemon twice.
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                text = learner.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = learner.howText.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = HOW_ALPHA),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val ARTWORK_SIZE = 72.dp

// The method is supporting text under the name, on a saturated ground where full white reads as loud
// as the name above it.
private const val HOW_ALPHA = 0.75f
