package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_priority
import tallgrass.shared.generated.resources.move_detail_target

/**
 * What the move does, in upstream's own words, with the two facts the prose never states.
 *
 * [effect] is PokeAPI's `effect_entries` and never `flavor_text_entries`, which is verbatim game text
 * #10 forbids shipping. It is null for 93 recent moves and the caller leaves this whole section out
 * rather than drawing an empty card — see `DECISIONS.md § A move's absent numbers are absent, not
 * zero`.
 *
 * Target and priority sit under it because neither is ever in the sentence: the prose says what
 * happens, not to whom or in what order.
 */
@Composable
fun MoveEffect(
    effect: String,
    targetText: UiText,
    priorityText: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = effect,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.appColors.textPrimary,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md),
        ) {
            Inset(
                label = Res.string.move_detail_target,
                value = targetText.asString(),
                modifier = Modifier.weight(1f),
            )
            Inset(
                label = Res.string.move_detail_priority,
                value = priorityText,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * The same raised pair the About tab draws height and weight in, which is where this pattern already
 * means "two short facts that belong together".
 */
@Composable
private fun Inset(
    label: StringResource,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.appColors.surfaceRaised)
                .padding(MaterialTheme.spacing.md),
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.appColors.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.appColors.textPrimary,
        )
    }
}
