package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.AbilityUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One ability in the list: a lettered tile, a name, a generation, and what it does.
 *
 * **A full-width row rather than the grid the other two lists use**, and the difference is the prose.
 * A dex card and a move card carry a name and a figure, which fit in half a screen; an ability is a
 * sentence, and a sentence in a half-width card wraps to four lines and stops being scannable. The
 * design file asks for these as rows for the same reason.
 *
 * **The tile is the same colour on every card.** The design tints it by category, and there is no
 * category: upstream has none for abilities, and three attempts at inventing one were measured and
 * rejected — see `DECISIONS.md § Rejected for now: a classification for abilities` and #65. Tinting
 * it by generation instead would put a colour on the card that means "when", which is not what a
 * reader scanning for an effect is looking for, and inventing a hue per generation is a taxonomy
 * wearing a palette. So the tile carries the initial and nothing else, which is a real job in an
 * alphabetical list of 314: it is what tells you where you are as you scroll.
 */
@Composable
fun AbilityCard(
    ability: AbilityUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(TILE_SIZE)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.appColors.accentSubtle),
            ) {
                Text(
                    text = ability.initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.appColors.onAccentSubtle,
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = ability.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.appColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Text(
                        text = ability.generationText.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.appColors.textTertiary,
                        maxLines = 1,
                    )
                }

                // Two lines, then an ellipsis. The longest short effect runs to three and a half
                // lines at this width, and a card that grows to fit the worst one leaves the rest of
                // the list with a hole under every short name. The detail is one tap away.
                Text(
                    text = ability.shortEffect,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                    maxLines = EFFECT_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
                )
            }
        }
    }
}

// The design file's figure, and it happens to be what two lines of the effect below it measure, so
// the tile and the text block end together on a card whose effect wraps.
private val TILE_SIZE = 44.dp

private const val EFFECT_LINES = 2
