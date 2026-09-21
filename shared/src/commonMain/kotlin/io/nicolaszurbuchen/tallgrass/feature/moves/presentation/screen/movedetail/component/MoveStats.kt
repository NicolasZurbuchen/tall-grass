package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveStatUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * Power, accuracy and PP as three raised tiles.
 *
 * **Figures rather than bars, which is a change from what this was.** A stat bar earns its length on
 * a Pokemon because the six are compared against each other and against every other Pokemon; these
 * three are not a set that means anything side by side — 90 power and 100 accuracy and 15 PP are
 * three different units, and drawing them as three lanes of the same length invited a comparison that
 * does not exist. As figures they read as what they are: the three numbers that decide whether to use
 * the move.
 *
 * The same raised tile the About tab on a Pokemon draws height and weight in, which is where this
 * pattern already means "short facts that belong together".
 *
 * A move with no power draws a dash. A status move does not deal a small amount of damage.
 */
@Composable
fun MoveStats(
    stats: List<MoveStatUiModel>,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = modifier.fillMaxWidth(),
    ) {
        stats.forEach { stat -> StatTile(stat = stat) }
    }
}

@Composable
private fun RowScope.StatTile(
    stat: MoveStatUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.appColors.surfaceRaised)
                .padding(MaterialTheme.spacing.md),
    ) {
        Text(
            text = stat.label.asString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.appColors.textSecondary,
        )
        Text(
            text = stat.valueText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.appColors.textPrimary,
        )
    }
}
