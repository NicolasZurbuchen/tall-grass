package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridScope
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.ENTRANCE_DONE
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.pop
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.TypeMatchupUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_stat_total
import tallgrass.shared.generated.resources.pokedex_detail_type_defenses

/**
 * The stats and matchups of the form on screen, which is the half of this screen that genuinely
 * moves when the switcher is used: Arceus is a different type in each of its eighteen forms.
 *
 * The bars grow to their values and the matchup chips pop in.
 */
@Composable
fun StatsTab(
    stats: StatsUiModel,
    tint: Color,
    modifier: Modifier = Modifier,
    elapsedMillis: Int = ENTRANCE_DONE,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        StatTable(stats = stats, tint = tint)

        Text(
            text = stringResource(Res.string.pokedex_detail_type_defenses),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.md),
        )

        MatchupFlow(matchups = stats.matchups, elapsedMillis = elapsedMillis)
    }
}

/**
 * Three columns: the names, the figures, and the lanes.
 *
 * DECISIONS.md § The stat table is a grid, so its columns measure themselves
 */
@OptIn(ExperimentalGridApi::class)
@Composable
private fun StatTable(
    stats: StatsUiModel,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    // Read before the config block rather than inside it: that block is not composable and runs
    // during the measure pass, where a MaterialTheme lookup is not available.
    val columnGap = MaterialTheme.spacing.lg
    val rowGap = MaterialTheme.spacing.md

    Grid(
        config = {
            column(GridTrackSize.Auto)
            column(GridTrackSize.Auto)
            column(1.fr)
            columnGap(columnGap)
            rowGap(rowGap)
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        stats.bars.forEach { bar ->
            StatCells(
                label = bar.label.asString(),
                valueText = bar.valueText,
                fraction = bar.fraction,
                style = MaterialTheme.typography.bodyMedium,
                labelColor = MaterialTheme.appColors.textSecondary,
                tint = tint,
            )
        }

        // The total's lane is the mean of the six above it, because a bar is full at 160 and this one
        // is full at six times that. Nothing else would let the two be compared down the column.
        StatCells(
            label = stringResource(Res.string.pokedex_detail_stat_total),
            valueText = stats.totalText,
            fraction = stats.totalFraction,
            style = MaterialTheme.typography.titleSmall,
            labelColor = MaterialTheme.appColors.textPrimary,
            tint = tint,
        )
    }
}

/**
 * One row of the table: a name, a figure, and a lane that grows to the figure.
 *
 * Functional rather than decorative: the length *is* the number, so this one keeps running under
 * reduced motion — Compose's own duration scaling shortens it to a frame, which is the right answer
 * for a movement that carries information. See #12 § Reduced motion.
 *
 * All seven start together. See `DECISIONS.md § The stat bars answer a form switch together`.
 */
@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridScope.StatCells(
    label: String,
    valueText: String,
    fraction: Float,
    style: TextStyle,
    labelColor: Color,
    tint: Color,
) {
    val grown by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = AppDuration.LONG, easing = AppEasing.EaseOutQuint),
        label = "statBar",
    )

    Text(
        text = label,
        style = style,
        color = labelColor,
        modifier = Modifier.gridItem(alignment = Alignment.CenterStart),
    )
    Text(
        text = valueText,
        style = style,
        color = MaterialTheme.appColors.textPrimary,
        modifier = Modifier.gridItem(alignment = Alignment.CenterEnd),
    )
    Box(
        modifier =
            Modifier
                .gridItem(alignment = Alignment.Center)
                .fillMaxWidth()
                .height(LANE_HEIGHT)
                .clip(RoundedCornerShape(LANE_HEIGHT))
                .background(MaterialTheme.appColors.borderSubtle),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(grown)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(LANE_HEIGHT))
                    .background(tint),
        )
    }
}

/**
 * Chips sized to their own text, wrapped onto as many rows as they need.
 *
 * DECISIONS.md § A matchup chip is sized by its name, not by the grid
 */
@Composable
private fun MatchupFlow(
    matchups: List<TypeMatchupUiModel>,
    elapsedMillis: Int,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = modifier.fillMaxWidth(),
    ) {
        // Staggered per chip rather than per row, because a flow does not report where it broke.
        // AppStagger's own cap holds eighteen of them under four hundred milliseconds, which is what
        // the row grouping was there to avoid.
        matchups.forEachIndexed { index, matchup ->
            MatchupChip(matchup = matchup, modifier = Modifier.pop(entranceFraction(index, elapsedMillis)))
        }
    }
}

@Composable
private fun MatchupChip(
    matchup: TypeMatchupUiModel,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColors

    // The chip is its type's colour twice over: a wash of it behind, and the same hue pushed off
    // that wash in front.
    val ground = lerp(colors.surface, matchup.typeColor, GROUND_TINT)
    val label = lerp(matchup.typeColor, if (colors.isDark) Color.White else Color.Black, LABEL_SHIFT)

    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        modifier =
            modifier
                .clip(MaterialTheme.shapes.small)
                .background(ground)
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    ) {
        Text(text = matchup.typeLabel, style = MaterialTheme.typography.bodyMedium, color = label)
        Text(text = matchup.factorText, style = MaterialTheme.typography.titleSmall, color = label)
    }
}

private val LANE_HEIGHT = 6.dp

// Enough of the type's colour for the chip to be identifiable at a glance, little enough that the
// label on top of it still has somewhere to go.
private const val GROUND_TINT = 0.18f

// How far the label moves off its own hue. The pure type colour measures 1.9:1 against a white
// sheet, which is not a contrast ratio so much as a suggestion.
private const val LABEL_SHIFT = 0.45f
