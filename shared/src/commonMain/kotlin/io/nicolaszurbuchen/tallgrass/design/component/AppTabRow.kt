package io.nicolaszurbuchen.tallgrass.design.component

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
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * A short rule under a left-aligned label, per tab.
 *
 * **Extracted at the third call site, which is the test `MoveDetailTabRow` wrote for itself**: two
 * copies is a coincidence, three is a shape. The Pokemon detail, the move detail and the ability
 * detail all draw this, and by the third one the cost of them drifting apart had overtaken the cost
 * of the adapter each call site needs.
 *
 * That adapter is the index. Each screen has its own tab enum — the tabs are that screen's
 * vocabulary and belong to it — so what crosses this boundary is a list of labels and a position in
 * it, which is the most a purely presentational component can know. The call sites map back with
 * `entries[index]`.
 */
@Composable
fun AppTabRow(
    tabs: List<UiText>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        modifier = modifier.fillMaxWidth(),
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable { onTabClick(index) }
                        .padding(horizontal = MaterialTheme.spacing.xs, vertical = MaterialTheme.spacing.sm),
            ) {
                Text(
                    text = label.asString(),
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
