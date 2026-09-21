package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveFactUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * What upstream recorded about how the move behaves: how many times it hits, what it inflicts and at
 * what odds, how much it drains.
 *
 * **The list is as long as the move has facts and no longer.** A move with no drain has no drain row,
 * because upstream's zero there means "no drain" rather than "drains none" — and a row per possible
 * mechanic would bury the two that matter under eight that say nothing. The same reasoning leaves
 * this section out entirely for the 92 moves with no meta row.
 *
 * Rows rather than chips, matching the About tab on a Pokemon: these are label-and-value facts, and
 * the eye reads a column of them faster than a wrapped field of pills.
 */
@Composable
fun MoveMechanics(
    facts: List<MoveFactUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        facts.forEach { fact ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.sm),
            ) {
                Text(
                    text = fact.label.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.width(LABEL_COLUMN_WIDTH),
                )
                Text(
                    text = fact.value.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textPrimary,
                )
            }
        }
    }
}

// Wide enough for "Critical hits", the longest label here, and deliberately the same figure the
// About tab on a Pokemon uses so the two screens line their values up in the same place. Duplicated
// rather than shared: it is a fact about these labels, and a token in design/ would be a number with
// no way to say which text it was measured against.
private val LABEL_COLUMN_WIDTH = 104.dp
