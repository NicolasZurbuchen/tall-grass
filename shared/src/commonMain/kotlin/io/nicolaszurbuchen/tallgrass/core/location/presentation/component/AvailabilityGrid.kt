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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_legend_no
import tallgrass.shared.generated.resources.location_detail_legend_yes

/**
 * Every game as a small cell, grouped into console rows.
 *
 * **One component, two directions.** A route asks which games have anything on it, a Pokemon asks
 * which games have it at all, and the grid is the same either way -- which is what #24 found when it
 * noticed the two screens were one component pointed opposite ways.
 *
 * **Colour encodes exactly one binary: obtainable or not.** #9 prototyped and rejected three richer
 * schemes -- segmenting a chip by method, a primary method plus pips, colour-by-method -- because a
 * Pokemon obtainable six ways must not look different from one obtainable once. The cell answers "can
 * I get it here", and nothing else.
 *
 * **Every cell stays tappable, including the grey ones.** That is how a negative gets confirmed at
 * the moment somebody asks, and it is what lets the two empty states be told apart: Scarlet says the
 * data is not available yet, and Black says transfer it in from another game.
 *
 * Grouping by console rather than by generation is the author's call in #9: *if I play Diamond, I may
 * not know it is the fourth generation, but I know I am holding a DS.* It also carries real weight,
 * because the codes are unique only within a row -- `Y` is Yellow on one and Pokemon Y on another.
 */
@Composable
fun AvailabilityGrid(
    grid: AvailabilityGridUiModel,
    onVersionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = modifier.fillMaxWidth(),
    ) {
        grid.rows.forEach { row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(row.console.label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.appColors.textTertiary,
                    modifier = Modifier.width(ROW_LABEL_WIDTH),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                    row.cells.forEach { cell ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .size(CELL_SIZE)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        if (cell.isAvailable) {
                                            AVAILABILITY_YES
                                        } else {
                                            MaterialTheme.appColors.surfaceRaised
                                        },
                                    ).clickable { onVersionClick(cell.slug) },
                        ) {
                            Text(
                                text = cell.code,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color =
                                    if (cell.isAvailable) Color.White else MaterialTheme.appColors.textTertiary,
                            )
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
        ) {
            LegendEntry(AVAILABILITY_YES, stringResource(Res.string.location_detail_legend_yes))
            LegendEntry(MaterialTheme.appColors.surfaceRaised, stringResource(Res.string.location_detail_legend_no))
        }
    }
}

@Composable
private fun LegendEntry(
    color: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Box(modifier = Modifier.size(LEGEND_SWATCH).clip(MaterialTheme.shapes.extraSmall).background(color))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.appColors.textTertiary,
        )
    }
}

// Wide enough for "GB / GBC", which is the longest row label.
private val ROW_LABEL_WIDTH = 56.dp

// Three characters at labelSmall, which is the widest code -- the six downloadable chapters take
// three so they can carry their parent game's two.
private val CELL_SIZE = 30.dp

private val LEGEND_SWATCH = 10.dp

// Off the type palette on purpose: this green means "yes" rather than "Grass", and reusing a type
// colour here would make an availability grid look like it was saying something about types.
//
// Shared with the breadcrumb, which draws the same token after the grid collapses into it. One value
// rather than two, because the whole point of the collapse is that the reader recognises the cell.
internal val AVAILABILITY_YES = Color(0xFF3FAE72)
