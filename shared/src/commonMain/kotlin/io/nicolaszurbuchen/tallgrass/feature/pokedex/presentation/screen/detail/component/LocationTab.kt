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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.location.presentation.component.AvailabilityBreadcrumb
import io.nicolaszurbuchen.tallgrass.core.location.presentation.component.AvailabilityGrid
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.asLabelColor
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.LocationUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantPlaceUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_available_in
import tallgrass.shared.generated.resources.pokedex_detail_catch_rate
import tallgrass.shared.generated.resources.pokedex_detail_where

/**
 * Where this Pokemon can be got: how, in which games, and where in the one chosen.
 *
 * **The pills come first and are the coarse answer**, because two of them are not encounter methods
 * at all. #9 dropped the aggregate "methods across all games" list for them: Pikachu's read *Tall
 * grass, Gift, Overworld, Headbutt tree, Horde, SOS call, In-game trade, Max Raid*, which is what
 * aggregating over 38 games does to information, and no list of methods can say "you cannot get this
 * here".
 *
 * Then catch rate, which #43 moved here from the About tab's Training block -- it is a fact about
 * meeting a Pokemon rather than about raising one, and under that heading it read as a stat you
 * improve. Both the figure and a word for it, because 45 means nothing without knowing 255 is the
 * maximum.
 *
 * Then the same availability grid the location detail draws, pointed the other way round, and **not
 * scoped to a region**: the question here is which of my games has this.
 *
 * **No method tabs and no condition selector**, which is #24's split. One Pokemon in one game is one
 * to three rows, so tabs would be chrome over nothing; a route yields twenty and needs both.
 */
@Composable
fun LocationTab(
    location: LocationUiModel,
    tint: Color,
    onVersionClick: (String) -> Unit,
    onBreadcrumbClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (location.captureMethods.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                location.captureMethods.forEach { method ->
                    Text(
                        text = stringResource(method.label),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier =
                            Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(method.color)
                                .padding(horizontal = PILL_PADDING_H, vertical = PILL_PADDING_V),
                    )
                }
            }
        }

        CatchRate(location = location, tint = tint)

        Text(
            text = stringResource(Res.string.location_detail_available_in),
            style = MaterialTheme.typography.headlineMedium,
            color = tint.asLabelColor(),
        )

        if (location.selected == null) {
            AvailabilityGrid(grid = location.grid, onVersionClick = onVersionClick)
        } else {
            AvailabilityBreadcrumb(
                cell = location.selected,
                summary = location.breadcrumbText ?: UiText.Raw(""),
                onClick = onBreadcrumbClick,
            )
        }

        location.emptyText?.let {
            Text(
                text = it.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.appColors.textSecondary,
            )
        }

        if (location.places.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.pokedex_detail_where),
                style = MaterialTheme.typography.headlineMedium,
                color = tint.asLabelColor(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                location.places.forEach { place ->
                    PlaceRow(place = place, tint = tint, onClick = { onPlaceClick(place.locationSlug) })
                }
            }
        }
    }
}

@Composable
private fun CatchRate(
    location: LocationUiModel,
    tint: Color,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.appColors.surfaceRaised)
                .padding(MaterialTheme.spacing.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(Res.string.pokedex_detail_catch_rate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.appColors.textSecondary,
            )
            Text(
                // Both halves: the figure for anyone who knows the scale, the word for everyone
                // else. #43 asks only for the second, and the first is the fact it is a reading of.
                text = "${location.catchRateText.asString()}  ${stringResource(location.catchDifficulty.label)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.appColors.textPrimary,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(BAR_HEIGHT)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.appColors.surface),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(location.catchFraction)
                        .height(BAR_HEIGHT)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(tint),
            )
        }
    }
}

@Composable
private fun PlaceRow(
    place: VariantPlaceUiModel,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.appColors.surfaceRaised)
                .clickable(onClick = onClick)
                .padding(MaterialTheme.spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.locationName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text =
                    listOfNotNull(
                        place.areaName,
                        place.levelText.asString(),
                        place.rateText?.asString(),
                        place.conditionText?.asString(),
                    ).joinToString(DETAIL_SEPARATOR),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
            )
        }

        Text(
            text = place.method.label.asString(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1,
            modifier =
                Modifier
                    .width(METHOD_PILL_WIDTH)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(tint)
                    .padding(horizontal = PILL_PADDING_H, vertical = PILL_PADDING_V),
        )
    }
}

private val PILL_PADDING_H = 10.dp
private val PILL_PADDING_V = 5.dp

// Thin enough to read as a measure behind the figure rather than as a component of its own, matching
// the encounter rows on the route side.
private val BAR_HEIGHT = 4.dp

// Fixed, so the method pills line up down the column instead of stepping in and out with the length
// of each label.
private val METHOD_PILL_WIDTH = 96.dp

private const val DETAIL_SEPARATOR = " · "
