package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.ConditionAxisUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_any
import tallgrass.shared.generated.resources.location_detail_conditions

/**
 * One row of chips per axis the table actually varies on.
 *
 * **Data-driven, so FireRed shows nothing here and HeartGold shows three rows** -- time, swarm and
 * radio. An axis with one possible value is left out upstream of this, because a control offering one
 * answer is asking a question nobody has.
 *
 * "Any" is a chip rather than the absence of one, and it is the default. It means *unpinned*: the
 * rows below show the best case across every state on that axis, which is what makes their rates read
 * "up to X%". Pin every axis and the figures become exact and sum to 100.
 *
 * **The whole selector darkens once anything is pinned**, so a reader can always tell at a glance
 * which regime the figures below are in. #24 chose that over a running total, which was noise as soon
 * as the "up to" convention made the two regimes explicit -- and which would have shown 130% on the
 * routes where upstream left day and night untagged.
 */
@Composable
fun ConditionSelector(
    axes: List<ConditionAxisUiModel>,
    onConditionClick: (axis: String, value: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (axes.isEmpty()) return

    val isPinned = axes.any { it.selected != null }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(
                    if (isPinned) MaterialTheme.appColors.accentSubtle else MaterialTheme.appColors.surfaceRaised,
                ).padding(MaterialTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.location_detail_conditions),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.appColors.textTertiary,
        )

        axes.forEach { axis ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = axis.label.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    Chip(
                        label = stringResource(Res.string.location_detail_any),
                        isSelected = axis.selected == null,
                        onClick = { onConditionClick(axis.axis, null) },
                    )

                    axis.options.forEach { option ->
                        Chip(
                            label = option.label.asString(),
                            isSelected = axis.selected == option.slug,
                            onClick = { onConditionClick(axis.axis, option.slug) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (isSelected) Color.White else MaterialTheme.appColors.textSecondary,
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .background(
                    if (isSelected) MaterialTheme.appColors.accent else MaterialTheme.appColors.surface,
                ).clickable(onClick = onClick)
                .padding(horizontal = CHIP_PADDING_H, vertical = CHIP_PADDING_V),
    )
}

private val CHIP_PADDING_H = 10.dp
private val CHIP_PADDING_V = 5.dp
