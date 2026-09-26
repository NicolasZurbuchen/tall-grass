package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.EncounterRowUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One Pokemon on a route: its artwork, its name, the levels it comes at, and how often.
 *
 * The bar behind the rate is **a share of a hundred, not of the commonest row**. Normalising it to
 * fill would say that whatever turns up most on a route is a certainty, which is exactly the thing a
 * rate is there to deny.
 *
 * A method with no meaningful rate -- a raid, a gift, a trade, an SOS call -- draws no figure and no
 * bar rather than a fabricated percentage. #9 settled that, and the row still carries the levels,
 * which is the part that is true either way.
 */
@Composable
fun EncounterRow(
    row: EncounterRowUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.appColors.surfaceRaised)
                .clickable(onClick = onClick)
                .padding(MaterialTheme.spacing.sm),
    ) {
        AsyncImage(
            model = row.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(SPRITE_SIZE),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = row.levelText.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
            )

            row.rateText?.let {
                Box(
                    modifier =
                        Modifier
                            .padding(top = MaterialTheme.spacing.xs)
                            .fillMaxWidth()
                            .height(BAR_HEIGHT)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.appColors.surface),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(row.rateFraction)
                                .height(BAR_HEIGHT)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(row.primaryType.color),
                    )
                }
            }
        }

        row.rateText?.let {
            Text(
                text = it.asString(),
                style = MaterialTheme.typography.bodyMedium,
                // Quieter while anything is unpinned, because the figure is a best case rather than
                // the rate: the wording says "up to" and the weight should not argue with it.
                color =
                    if (row.isBestCase) MaterialTheme.appColors.textSecondary else MaterialTheme.appColors.textPrimary,
                maxLines = 1,
            )
        }
    }
}

private val SPRITE_SIZE = 44.dp

// Thin enough to read as a measure behind the words rather than as a component of its own.
private val BAR_HEIGHT = 4.dp
