package io.nicolaszurbuchen.tallgrass.core.location.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityCellUiModel
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * What the grid collapses into once a game is chosen: the cell's own token, the game's name, and a
 * line saying what is behind it.
 *
 * **Tapping it opens the grid again**, which is the part of #9 that actually solved the problem. The
 * earlier layouts all scrolled the selector away and the reader lost their place changing games; here
 * the selector is never more than one tap from where they are looking, and neither state scrolls.
 *
 * [summary] is the line under the name and says what the reader is about to see -- how many species
 * are here, or that there are none. It is the caller's to write, because the route side counts
 * species and the Pokemon side counts places.
 */
@Composable
fun AvailabilityBreadcrumb(
    cell: AvailabilityCellUiModel,
    summary: UiText,
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
                .padding(MaterialTheme.spacing.md),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(TOKEN_SIZE)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(
                        if (cell.isAvailable) AVAILABILITY_YES else MaterialTheme.appColors.surface,
                    ),
        ) {
            Text(
                text = cell.code,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = if (cell.isAvailable) Color.White else MaterialTheme.appColors.textTertiary,
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = cell.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = summary.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
                maxLines = 1,
            )
        }
    }
}

// The same token the grid draws, a size up: it is the thing that travelled here from the cell, so it
// has to be recognisably the same object.
private val TOKEN_SIZE = 34.dp
