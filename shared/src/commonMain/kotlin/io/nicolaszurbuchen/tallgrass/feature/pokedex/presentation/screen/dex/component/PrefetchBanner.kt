package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.uimodel.PrefetchUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One line at the foot of the dex while the artwork is being saved.
 *
 * **Deliberately not a gate and deliberately not dismissible.** Nothing on this screen is waiting
 * for it — every card is already drawn from the bundled dataset — so it announces an improvement
 * rather than a state, and there is no decision for the reader to make about it. It leaves when the
 * run does.
 *
 * A determinate bar rather than a spinner, because the run has a known length and a spinner over a
 * background job would read as the screen being busy.
 */
@Composable
fun PrefetchBanner(
    prefetch: PrefetchUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.appColors.surfaceRaised,
        contentColor = MaterialTheme.appColors.textSecondary,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        ) {
            Text(
                text = prefetch.message.asString(),
                style = MaterialTheme.typography.bodySmall,
            )

            if (prefetch.fraction != null) {
                LinearProgressIndicator(
                    progress = { prefetch.fraction },
                    color = MaterialTheme.appColors.accent,
                    trackColor = MaterialTheme.appColors.borderSubtle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
