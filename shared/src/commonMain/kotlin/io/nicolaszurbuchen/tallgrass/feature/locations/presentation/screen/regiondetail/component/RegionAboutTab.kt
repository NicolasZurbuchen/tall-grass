package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.asLabelColor
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionAboutUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.region_detail_details
import tallgrass.shared.generated.resources.region_detail_games
import tallgrass.shared.generated.resources.region_detail_introduced
import tallgrass.shared.generated.resources.region_detail_native_name
import tallgrass.shared.generated.resources.region_detail_stat_games
import tallgrass.shared.generated.resources.region_detail_stat_locations
import tallgrass.shared.generated.resources.region_detail_stat_pokedex

/**
 * Three figures, a paragraph, then the facts a wiki would list.
 *
 * The blurb is original prose rather than the region's own description, which would be flavour text
 * and is the one thing #10 forbids shipping. Eleven of them is the whole cost of not needing it.
 *
 * [tint] is the region's colour, and the heading carries it for the same reason the Pokemon detail's
 * do: the sheet is part of the screen above it rather than a white page that arrived separately.
 */
@Composable
fun RegionAboutTab(
    about: RegionAboutUiModel,
    tint: Color,
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
            Figure(Res.string.region_detail_stat_pokedex, about.pokedexText, Modifier.weight(1f))
            Figure(Res.string.region_detail_stat_locations, about.locationsText, Modifier.weight(1f))
            Figure(Res.string.region_detail_stat_games, about.gamesText, Modifier.weight(1f))
        }

        Text(
            text = about.blurb,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.padding(top = MaterialTheme.spacing.lg),
        )

        Text(
            text = stringResource(Res.string.region_detail_details),
            style = MaterialTheme.typography.headlineMedium,
            color = tint.asLabelColor(),
            modifier = Modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.sm),
        )

        KeyValueRow(Res.string.region_detail_introduced, about.introducedText)

        // Absent for Orre, which upstream has no Japanese name for, and the row goes rather than
        // showing a dash: a dash in a column of words reads as a value nobody filled in.
        about.nativeName?.let { KeyValueRow(Res.string.region_detail_native_name, UiText.Raw(it)) }

        KeyValueRow(Res.string.region_detail_games, UiText.Raw(about.gamesList))
    }
}

@Composable
private fun Figure(
    label: StringResource,
    value: UiText,
    modifier: Modifier = Modifier,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = value.asString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.appColors.textPrimary,
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.appColors.textSecondary,
        )
    }
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
// the length of each label. The same width the Pokemon detail's About tab uses.
private val LABEL_COLUMN_WIDTH = 104.dp
