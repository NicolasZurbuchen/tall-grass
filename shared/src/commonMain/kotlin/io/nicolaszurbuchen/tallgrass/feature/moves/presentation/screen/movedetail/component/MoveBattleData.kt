package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridScope
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveBarUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * Power, accuracy and PP as three lanes.
 *
 * The same table a Pokemon's base stats are drawn in, down to the grid that measures its own columns
 * and the bars that grow to their values — these are numbers a reader compares between moves, and
 * comparing them is what a length is for. See
 * `DECISIONS.md § The stat table is a grid, so its columns measure themselves`.
 *
 * A move with no power draws a dash and an empty lane, which is the honest picture: a status move
 * does not deal a small amount of damage.
 */
@OptIn(ExperimentalGridApi::class)
@Composable
fun MoveBattleData(
    bars: List<MoveBarUiModel>,
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
        bars.forEach { bar ->
            BarCells(
                label = bar.label.asString(),
                valueText = bar.valueText,
                fraction = bar.fraction,
                tint = tint,
            )
        }
    }
}

/**
 * One row: a name, a figure, and a lane that grows to the figure.
 *
 * Functional rather than decorative, so it keeps running under reduced motion for the same reason the
 * stat bars do — the length *is* the number. See #12 § Reduced motion.
 */
@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridScope.BarCells(
    label: String,
    valueText: String,
    fraction: Float,
    tint: Color,
) {
    val grown by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = AppDuration.LONG, easing = AppEasing.EaseOutQuint),
        label = "moveBar",
    )

    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.appColors.textSecondary,
        modifier = Modifier.gridItem(alignment = Alignment.CenterStart),
    )
    Text(
        text = valueText,
        style = MaterialTheme.typography.bodyMedium,
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

private val LANE_HEIGHT = 6.dp
