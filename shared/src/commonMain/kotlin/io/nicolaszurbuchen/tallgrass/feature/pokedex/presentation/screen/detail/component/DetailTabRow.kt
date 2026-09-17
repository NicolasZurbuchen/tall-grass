package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

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
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * Written against the enum rather than against Material's `TabRow`, because the underline here is a
 * short rule under the label rather than a full-width indicator, and the row stays left-aligned as
 * Location and Moves join it instead of redistributing every tab.
 */
@Composable
fun DetailTabRow(
    selected: DetailTabUiModel,
    onTabClick: (DetailTabUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        DetailTabUiModel.entries.forEach { tab ->
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
