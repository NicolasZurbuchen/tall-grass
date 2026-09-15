package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

@Composable
fun DexCard(
    name: String,
    numberText: String,
    formLabel: String?,
    artworkUrl: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(CARD_ASPECT_RATIO),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = tint, contentColor = Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = numberText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = NUMBER_ALPHA),
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = artworkUrl,
                    // The name is read out immediately below, so describing the artwork too would
                    // have a screen reader say every Pokemon twice.
                    contentDescription = null,
                    modifier = Modifier.size(ARTWORK_SIZE),
                )
            }

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (formLabel != null) {
                    Text(
                        text = formLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = NUMBER_ALPHA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// Slightly taller than square, so a two-line card -- one with a form chip -- does not have to grow
// and leave the row it sits in ragged.
private const val CARD_ASPECT_RATIO = 0.82f

private val ARTWORK_SIZE = 84.dp

// The number and the form label are supporting text on a saturated ground, where full white reads
// as loud as the name it sits under.
private const val NUMBER_ALPHA = 0.7f
