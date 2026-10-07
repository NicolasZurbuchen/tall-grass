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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypeIcon
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.ENTRANCE_DONE
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.asLabelColor
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.pop
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.MatchupGroupUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatsUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_resistances
import tallgrass.shared.generated.resources.pokedex_detail_stat_max
import tallgrass.shared.generated.resources.pokedex_detail_stat_min
import tallgrass.shared.generated.resources.pokedex_detail_stat_total
import tallgrass.shared.generated.resources.pokedex_detail_weaknesses

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

        // Under the table rather than in it: Dynamaxing multiplies the HP a Pokemon already has, so
        // there is nothing to put in the other four columns and a seventh row would claim otherwise.
        stats.dynamaxHpText?.let { text ->
            Text(
                text = text.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md),
            )
        }

        // Two sections rather than eighteen chips in one flow. A reader sizing a Pokemon up asks
        // what gets through it and what bounces off, and those are different questions -- read as
        // one list they had to be told apart by the factor printed on each chip.
        MatchupSection(
            title = Res.string.pokedex_detail_weaknesses,
            groups = stats.weaknesses,
            tint = tint,
            firstChipIndex = 0,
            elapsedMillis = elapsedMillis,
        )

        // The stagger runs on across the two, so it reads as one entrance rather than two that
        // happen to start together.
        MatchupSection(
            title = Res.string.pokedex_detail_resistances,
            groups = stats.resistances,
            tint = tint,
            firstChipIndex = stats.weaknesses.sumOf { it.types.size },
            elapsedMillis = elapsedMillis,
        )
    }
}

/**
 * Five columns: the names, the base figures, the lanes, and the two ends of what each stat reaches
 * on a level 100 Pokemon.
 *
 * The lane is the only flexible track, so the four text columns measure themselves and it takes what
 * is left. The column gap is a step down from the rest of the sheet because four gaps across a phone
 * would otherwise come out of the lane, which is the part carrying the comparison.
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
    val columnGap = MaterialTheme.spacing.md
    val rowGap = MaterialTheme.spacing.md

    Grid(
        config = {
            column(GridTrackSize.Auto)
            column(GridTrackSize.Auto)
            column(1.fr)
            column(GridTrackSize.Auto)
            column(GridTrackSize.Auto)
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
                minText = bar.minText,
                maxText = bar.maxText,
                style = MaterialTheme.typography.bodyMedium,
                labelColor = MaterialTheme.appColors.textSecondary,
                tint = tint,
            )
        }

        // The total's lane is the mean of the six above it, because a bar is full at 160 and this one
        // is full at six times that. Nothing else would let the two be compared down the column.
        //
        // Its range is the two columns added up. See `StatsUiModel` on why no Pokemon reaches either
        // end of it.
        StatCells(
            label = stringResource(Res.string.pokedex_detail_stat_total),
            valueText = stats.totalText,
            fraction = stats.totalFraction,
            minText = stats.totalMinText,
            maxText = stats.totalMaxText,
            style = MaterialTheme.typography.titleSmall,
            labelColor = MaterialTheme.appColors.textPrimary,
            tint = tint,
        )

        // Under the columns rather than over them, because the table is read down the left and these
        // two are a footnote to the right-hand pair rather than headings the rows hang off.
        RangeLegendCells()
    }
}

/**
 * The words "Min" and "Max" under the two columns they belong to, and four empty cells to put them
 * there.
 *
 * The blanks are how a grid says "this row starts in column four"; there is no skip.
 */
@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridScope.RangeLegendCells() {
    repeat(LEGEND_LEADING_CELLS) { Box(modifier = Modifier.gridItem()) }

    listOf(Res.string.pokedex_detail_stat_min, Res.string.pokedex_detail_stat_max).forEach { label ->
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.appColors.textTertiary,
            modifier = Modifier.gridItem(alignment = Alignment.CenterEnd),
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
    minText: String,
    maxText: String,
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

    // Quieter than the base figure on purpose. The base stat is what the row is about and what the
    // lane draws; these two are the scale it turns into, and reading as loud would make three
    // numbers competing rather than one with its bounds.
    listOf(minText, maxText).forEach { text ->
        Text(
            text = text,
            style = style,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.gridItem(alignment = Alignment.CenterEnd),
        )
    }
}

/**
 * One half of the defences: a heading, then a row per multiplier.
 *
 * Absent rather than empty-stated when there is nothing in it, which is a real case at both ends --
 * Eelektross is weak to nothing and Normal resists nothing. A heading over no chips would read as a
 * read that had not landed.
 *
 * [firstChipIndex] is where this section falls in the tab's stagger, so the two sections enter as one
 * sequence rather than as two starting at once.
 */
@Composable
private fun MatchupSection(
    title: StringResource,
    groups: List<MatchupGroupUiModel>,
    tint: Color,
    firstChipIndex: Int,
    elapsedMillis: Int,
    modifier: Modifier = Modifier,
) {
    if (groups.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.headlineMedium,
            color = tint.asLabelColor(),
            modifier = Modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.sm),
        )

        var chipIndex = firstChipIndex

        groups.forEach { group ->
            MatchupRow(group = group, firstChipIndex = chipIndex, elapsedMillis = elapsedMillis)
            chipIndex += group.types.size
        }
    }
}

/**
 * One multiplier and everything that hits for it: the factor on the left the way the breeding block
 * puts its labels there, and chips sized to their own text wrapping beside it.
 *
 * DECISIONS.md § A matchup chip is sized by its name, not by the grid
 */
@Composable
private fun MatchupRow(
    group: MatchupGroupUiModel,
    firstChipIndex: Int,
    elapsedMillis: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        modifier = modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xs),
    ) {
        Text(
            text = group.factorText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.width(FACTOR_COLUMN_WIDTH).padding(top = FACTOR_DROP),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Staggered per chip rather than per row, because a flow does not report where it broke.
            // AppStagger's own cap holds eighteen of them under four hundred milliseconds, which is
            // what the row grouping was there to avoid.
            group.types.forEachIndexed { offset, type ->
                MatchupChip(
                    type = type,
                    modifier = Modifier.pop(entranceFraction(firstChipIndex + offset, elapsedMillis)),
                )
            }
        }
    }
}

@Composable
private fun MatchupChip(
    type: TypeUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        modifier =
            modifier
                .clip(RoundedCornerShape(CHIP_CORNER))
                .background(type.color)
                .padding(horizontal = CHIP_PADDING_HORIZONTAL, vertical = CHIP_PADDING_VERTICAL),
    ) {
        TypeIcon(type = type, color = CHIP_CONTENT, modifier = Modifier.size(GLYPH_SIZE))
        Text(text = type.label, style = MaterialTheme.typography.labelSmall, color = CHIP_CONTENT)
    }
}

private val LANE_HEIGHT = 6.dp

// Name, figure and lane, which the legend has nothing to say about.
private const val LEGEND_LEADING_CELLS = 3

// White on all eighteen grounds rather than chosen per type, which is a deliberate trade and not an
// oversight: it is uniform, and on the paler types it is under the 4.5:1 a label would otherwise
// want. DECISIONS.md § A matchup chip is filled with its type's colour, and its label is white
private val CHIP_CONTENT = Color.White

// Larger than any radius in the shape scale, because a chip is a stadium rather than a rounded box.
// The same figure TypePill uses, so the two read as the same object at different sizes.
private val CHIP_CORNER = 999.dp

// Off the spacing scale, as TypePill's are and for the same reason: a stadium curves away from its
// contents at both ends, so a pill padded to the scale has visibly less room than a box would.
private val CHIP_PADDING_HORIZONTAL = 10.dp
private val CHIP_PADDING_VERTICAL = 6.dp

// Square vectors, sized against an 11sp label: large enough to be a symbol rather than a speck,
// small enough that the name is still what the chip is read by.
private val GLYPH_SIZE = 16.dp

// Narrower than the About tab's label column, which holds words. This one holds "×4" and everything
// past it would come out of the chips.
private val FACTOR_COLUMN_WIDTH = 36.dp

// The factor sits against the first row of chips rather than against the top of the block, which is
// a few pixels lower because a chip has padding and a bare line of text does not.
private val FACTOR_DROP = 6.dp
