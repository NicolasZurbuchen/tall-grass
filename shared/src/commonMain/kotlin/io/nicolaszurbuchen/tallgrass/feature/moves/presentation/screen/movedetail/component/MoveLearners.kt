package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveLearnerUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_no_learners

/**
 * Every Pokemon that learns the move, in National Dex order with a species' forms adjacent.
 *
 * **A grid of its own rather than the horizontal strip the design file drew.** A strip works for the
 * four the prototype had; Rest is learned by 1,213 Pokemon and Tackle by 409, and a row you scroll
 * sideways through 1,213 times is a worse answer than one you scroll down. The median move has 26.
 *
 * Three across rather than the dex's two: there is no name-and-pills block here, only a picture and a
 * line, so the card can be a third of the width and the list a third as long.
 */
@Composable
fun MoveLearners(
    learners: List<MoveLearnerUiModel>,
    onLearnerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (learners.isEmpty()) {
        // The 106 moves nobody is taught. Said out loud, because an empty tab reads as a read that
        // has not finished rather than as an answer.
        Text(
            text = stringResource(Res.string.move_detail_no_learners),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.appColors.textSecondary,
            modifier = modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.lg),
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(LEARNER_GRID_COLUMNS),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = modifier.fillMaxSize(),
    ) {
        items(items = learners, key = { it.slug }) { learner ->
            LearnerCard(learner = learner, onClick = { onLearnerClick(learner.slug) })
        }
    }
}

@Composable
private fun LearnerCard(
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

// Three across, which is what a card holding a picture and two short lines wants. The dex uses two
// because its cards also carry a name, a number and up to two pills.
private const val LEARNER_GRID_COLUMNS = 3

private val ARTWORK_SIZE = 72.dp

// The method is supporting text under the name, on a saturated ground where full white reads as loud
// as the name above it.
private const val HOW_ALPHA = 0.75f
