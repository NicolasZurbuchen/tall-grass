package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.AboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.GenderUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.ic_female
import tallgrass.shared.generated.resources.ic_male
import tallgrass.shared.generated.resources.pokedex_detail_base_exp
import tallgrass.shared.generated.resources.pokedex_detail_breeding
import tallgrass.shared.generated.resources.pokedex_detail_egg_cycle
import tallgrass.shared.generated.resources.pokedex_detail_egg_groups
import tallgrass.shared.generated.resources.pokedex_detail_ev_yield
import tallgrass.shared.generated.resources.pokedex_detail_female
import tallgrass.shared.generated.resources.pokedex_detail_gender
import tallgrass.shared.generated.resources.pokedex_detail_genderless
import tallgrass.shared.generated.resources.pokedex_detail_growth
import tallgrass.shared.generated.resources.pokedex_detail_height
import tallgrass.shared.generated.resources.pokedex_detail_male
import tallgrass.shared.generated.resources.pokedex_detail_training
import tallgrass.shared.generated.resources.pokedex_detail_weight

/**
 * Breeding belongs to the species and does not move when the switcher is used; the measurements and
 * the training figures belong to the form on screen. That is the rule the Species/Variant split
 * exists to keep. See #5 and `AboutUiModel`.
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
        GenderRow(gender = about.gender)
        KeyValueRow(label = Res.string.pokedex_detail_egg_groups, value = about.eggGroupsText)
        KeyValueRow(label = Res.string.pokedex_detail_egg_cycle, value = about.eggCycleText)

        SectionTitle(title = Res.string.pokedex_detail_training)

        // Absent for the 49 forms upstream has not costed yet -- every one of them a Legends Z-A
        // Mega. The rows go rather than showing a dash, because a dash in a column of figures reads
        // as a figure of zero.
        about.evYieldText?.let { KeyValueRow(label = Res.string.pokedex_detail_ev_yield, value = it) }
        about.baseExperienceText?.let { KeyValueRow(label = Res.string.pokedex_detail_base_exp, value = it) }

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

/**
 * The gender row, which is a [KeyValueRow] everywhere except in its value.
 *
 * A share is drawn as its symbol and its percentage rather than as a sentence, because the row is
 * read as a pair of numbers and a sentence makes them read as prose. Genderless has no share and
 * falls back to the sentence, which is the whole of what there is to say.
 */
@Composable
private fun GenderRow(
    gender: GenderUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(Res.string.pokedex_detail_gender),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.width(LABEL_COLUMN_WIDTH),
        )

        when (gender) {
            GenderUiModel.Genderless -> {
                Text(
                    text = stringResource(Res.string.pokedex_detail_genderless),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textPrimary,
                )
            }

            is GenderUiModel.Split -> {
                GenderShare(
                    symbol = Res.drawable.ic_male,
                    name = Res.string.pokedex_detail_male,
                    value = gender.maleText,
                    color = MALE_COLOR,
                )
                GenderShare(
                    symbol = Res.drawable.ic_female,
                    name = Res.string.pokedex_detail_female,
                    value = gender.femaleText,
                    color = FEMALE_COLOR,
                )
            }
        }
    }
}

/**
 * The symbol is the label here, so unlike the artwork elsewhere in this feature it is described:
 * there is no word "male" on the row for a screen reader to find instead.
 */
@Composable
private fun GenderShare(
    symbol: DrawableResource,
    name: StringResource,
    value: UiText,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(symbol),
            contentDescription = stringResource(name),
            tint = color,
            modifier = Modifier.size(SYMBOL_SIZE),
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

// Two lines of a stroked glyph, which needs a little more room than a filled one to stay a symbol
// rather than a smudge.
private val SYMBOL_SIZE = 18.dp

// Off the palette on purpose. These two are not this app's colours -- they are the convention every
// Pokedex before it has used, and a reader picks the row out by them rather than by reading it.
private val MALE_COLOR = Color(0xFF5C9DE8)
private val FEMALE_COLOR = Color(0xFFE87CA8)
