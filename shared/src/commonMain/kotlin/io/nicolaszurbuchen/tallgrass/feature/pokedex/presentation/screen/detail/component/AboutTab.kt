package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.AboutUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_breeding
import tallgrass.shared.generated.resources.pokedex_detail_egg_cycle
import tallgrass.shared.generated.resources.pokedex_detail_egg_groups
import tallgrass.shared.generated.resources.pokedex_detail_gender
import tallgrass.shared.generated.resources.pokedex_detail_growth
import tallgrass.shared.generated.resources.pokedex_detail_height
import tallgrass.shared.generated.resources.pokedex_detail_training
import tallgrass.shared.generated.resources.pokedex_detail_weight

/**
 * Height and weight belong to the form on screen; everything under them belongs to the species and
 * does not move when the switcher is used. That is the rule the Species/Variant split exists to
 * keep, so it is worth seeing in the order of the rows. See #5.
 *
 * Abilities are deliberately absent — they live in the Moves tab. See #27.
 */
@Composable
fun AboutTab(
    about: AboutUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.appColors.surfaceRaised)
                    .padding(MaterialTheme.spacing.md),
        ) {
            Measurement(label = Res.string.pokedex_detail_height, value = about.heightText, modifier = Modifier.weight(1f))
            Measurement(label = Res.string.pokedex_detail_weight, value = about.weightText, modifier = Modifier.weight(1f))
        }

        SectionTitle(title = Res.string.pokedex_detail_breeding)
        KeyValueRow(label = Res.string.pokedex_detail_gender, value = about.genderText)
        KeyValueRow(label = Res.string.pokedex_detail_egg_groups, value = about.eggGroupsText)
        KeyValueRow(label = Res.string.pokedex_detail_egg_cycle, value = about.eggCycleText)

        SectionTitle(title = Res.string.pokedex_detail_training)
        KeyValueRow(label = Res.string.pokedex_detail_growth, value = about.growthText)
    }
}

@Composable
private fun Measurement(
    label: StringResource,
    value: UiText,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.appColors.textSecondary,
        )
        Text(
            text = value.asString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.appColors.textPrimary,
        )
    }
}

@Composable
private fun SectionTitle(
    title: StringResource,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.appColors.textPrimary,
        modifier = modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.sm),
    )
}

@Composable
private fun KeyValueRow(
    label: StringResource,
    value: UiText,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        modifier = modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.width(LABEL_COLUMN_WIDTH),
        )
        Text(
            text = value.asString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.textPrimary,
        )
    }
}

// Fixed rather than measured, so the values line up down the tab instead of stepping in and out with
// the length of each label.
private val LABEL_COLUMN_WIDTH = 104.dp
