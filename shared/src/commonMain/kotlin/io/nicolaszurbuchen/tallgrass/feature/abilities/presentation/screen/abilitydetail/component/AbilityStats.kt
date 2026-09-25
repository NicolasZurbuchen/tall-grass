package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.component

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
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityStatUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * Generation, holders and hidden holders as three raised tiles.
 *
 * The same tile a move's three figures sit in, and the same tile the About tab on a Pokemon draws
 * height and weight in, which is where this pattern already means "short facts that belong together".
 * Duplicated rather than shared: a feature may only import from its own subtree, so the alternative
 * is a component in `design/` that takes a list of label-value pairs — which is `AppTabRow`'s trade
 * made for something with one call site less. Worth extracting when a fourth screen wants it.
 */
@Composable
fun AbilityStats(
    stats: List<AbilityStatUiModel>,
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
    stat: AbilityStatUiModel,
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
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.appColors.textPrimary,
        )
    }
}
