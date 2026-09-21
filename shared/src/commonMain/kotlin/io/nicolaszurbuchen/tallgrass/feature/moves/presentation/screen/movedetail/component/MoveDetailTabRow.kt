package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * The same short rule under a left-aligned label that a Pokemon's detail draws, written against this
 * screen's own enum.
 *
 * Duplicated from `DetailTabRow` rather than shared, and the duplication is the smaller cost: sharing
 * it means a component in `design/` that takes a label and a selected index, at which point it knows
 * less than either copy and both call sites grow an adapter. If a third screen wants tabs, that is
 * the moment to extract one — three call sites is evidence, two is a coincidence.
 */
@Composable
fun MoveDetailTabRow(
    selected: MoveDetailTabUiModel,
    onTabClick: (MoveDetailTabUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        modifier = modifier.fillMaxWidth(),
    ) {
        MoveDetailTabUiModel.entries.forEach { tab ->
            val isSelected = tab == selected

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable { onTabClick(tab) }
                        .padding(horizontal = MaterialTheme.spacing.xs, vertical = MaterialTheme.spacing.sm),
            ) {
                Text(
                    text = tab.label.asString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textTertiary,
                )

                Box(
                    modifier =
                        Modifier
                            .padding(top = MaterialTheme.spacing.xs)
                            .width(INDICATOR_WIDTH)
                            .height(INDICATOR_HEIGHT)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .then(
                                if (isSelected) {
                                    Modifier.background(MaterialTheme.appColors.textPrimary)
                                } else {
                                    Modifier
                                },
                            ),
                )
            }
        }
    }
}

private val INDICATOR_WIDTH = 24.dp
private val INDICATOR_HEIGHT = 3.dp
