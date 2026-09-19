package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.AppStagger
import io.nicolaszurbuchen.tallgrass.design.theme.ENTRANCE_DONE
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.pop
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatBarUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.TypeMatchupUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_stat_total
import tallgrass.shared.generated.resources.pokedex_detail_type_defenses
import tallgrass.shared.generated.resources.pokedex_detail_type_defenses_hint

/**
 * The stats and matchups of the form on screen, which is the half of this screen that genuinely
 * moves when the switcher is used: Arceus is a different type in each of its eighteen forms.
 *
 * The bars grow to their values and the matchup chips pop in, both staggered by the same step as
 * every other entrance in the app.
 */
@Composable
fun StatsTab(
    stats: StatsUiModel,
    tint: Color,
    modifier: Modifier = Modifier,
    elapsedMillis: Int = ENTRANCE_DONE,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        stats.bars.forEachIndexed { index, bar -> StatRow(bar = bar, tint = tint, barIndex = index) }

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.pokedex_detail_stat_total),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.width(STAT_LABEL_WIDTH),
            )
            Text(
                text = stats.totalText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.appColors.textPrimary,
            )
        }

        Text(
            text = stringResource(Res.string.pokedex_detail_type_defenses),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.xs),
        )
        Text(
            text = stringResource(Res.string.pokedex_detail_type_defenses_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
        )

        MatchupGrid(matchups = stats.matchups, elapsedMillis = elapsedMillis)
    }
}

/**
 * The bar grows to its value rather than appearing at it.
 *
 * Functional rather than decorative: the length *is* the number, so this one keeps running under
 * reduced motion — Compose's own duration scaling shortens it to a frame, which is the right answer
 * for a movement that carries information. See #12 § Reduced motion.
 */
@Composable
private fun StatRow(
    bar: StatBarUiModel,
    tint: Color,
    barIndex: Int,
    modifier: Modifier = Modifier,
) {
    val grown by animateFloatAsState(
        targetValue = bar.fraction,
        animationSpec =
            tween(
                durationMillis = AppDuration.LONG,
                delayMillis = AppStagger.delayFor(barIndex),
                easing = AppEasing.EaseOutQuint,
            ),
        label = "statBar",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xs),
    ) {
        Text(
            text = bar.label.asString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.width(STAT_LABEL_WIDTH),
        )
        Text(
            text = bar.valueText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.textPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(STAT_VALUE_WIDTH),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
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
}

@Composable
private fun MatchupGrid(
    matchups: List<TypeMatchupUiModel>,
    elapsedMillis: Int,
    modifier: Modifier = Modifier,
) {
    // Chunked into rows by hand rather than drawn in a LazyVerticalGrid: this sits inside a column
    // that already scrolls, and nesting a scroller of the same direction inside one is unmeasurable.
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        modifier = modifier.fillMaxWidth(),
    ) {
        matchups.chunked(MATCHUPS_PER_ROW).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                // Staggered by row rather than by chip. Eighteen chips popping one at a time is
                // nearly a second of the reader watching a grid assemble itself.
                row.forEach { matchup ->
                    MatchupChip(
                        matchup = matchup,
                        modifier = Modifier.weight(1f).pop(entranceFraction(rowIndex, elapsedMillis)),
                    )
                }

                // Keeps the last row's chips the width of every other row's.
                repeat(MATCHUPS_PER_ROW - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MatchupChip(
    matchup: TypeMatchupUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        modifier =
            modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .background(matchup.typeColor)
                .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    ) {
        Text(
            text = matchup.typeLabel,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text = matchup.factorText,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            modifier = Modifier.wrapContentWidth(),
        )
    }
}

private val STAT_LABEL_WIDTH = 72.dp
private val STAT_VALUE_WIDTH = 32.dp
private val LANE_HEIGHT = 6.dp

// Three across, like the dex grid, so a full eighteen fits in six rows without a scroller.
private const val MATCHUPS_PER_ROW = 3
