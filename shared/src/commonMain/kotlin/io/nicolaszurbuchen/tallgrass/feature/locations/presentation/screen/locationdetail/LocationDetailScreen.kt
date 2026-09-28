package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.core.location.presentation.component.AvailabilityBreadcrumb
import io.nicolaszurbuchen.tallgrass.core.location.presentation.component.AvailabilityGrid
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppScreenHeader
import io.nicolaszurbuchen.tallgrass.design.component.AppTabRow
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.component.ConditionSelector
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.component.EncounterRow
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.component.LocationDetailSkeleton
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_available_in

/**
 * One place, and what can be met there in each game it appears in.
 *
 * **The grid collapses into a breadcrumb rather than scrolling away.** Tap a cell and the games fold
 * into one line carrying that cell's token, with the encounters underneath; tap the line and the
 * games come back. #9 rejected a tall strip, era tabs plus chips, a sticky rail and a 23-row
 * accordion before arriving here, and the reason this one works is that the selector is never more
 * than one tap from whatever the reader is looking at.
 *
 * **Method tabs belong here and not on the Pokemon side.** One route in one game yields up to five
 * tables -- Berry Forest in FireRed shows Tall grass, Surf and all three rods -- where one Pokemon in
 * one game yields one to three rows and tabs would be chrome over nothing. Each tab is its own 100%,
 * which is why they are the actual methods and never a "Fishing" group.
 */
@Composable
fun LocationDetailScreen(
    state: LocationDetailUiModel,
    onVersionClick: (String) -> Unit,
    onBreadcrumbClick: () -> Unit,
    onMethodClick: (String) -> Unit,
    onConditionClick: (axis: String, value: String?) -> Unit,
    onPokemonClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        AppScreenHeader(title = state.name, onBackClick = onBackClick)

        Text(
            text = state.regionName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.error != null -> {
                    AppErrorBanner(
                        text = state.error.title,
                        icon = state.error.icon,
                        onRetry = onRetryClick,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.isLoading -> {
                    LocationDetailSkeleton()
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            if (state.selected == null) {
                                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                                    Text(
                                        text = stringResource(Res.string.location_detail_available_in),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.appColors.textPrimary,
                                    )
                                    AvailabilityGrid(grid = state.grid, onVersionClick = onVersionClick)
                                }
                            } else {
                                AvailabilityBreadcrumb(
                                    cell = state.selected,
                                    // Blank only for the beat between tapping a cell and the rows landing.
                                    summary = state.breadcrumbText ?: UiText.Raw(""),
                                    onClick = onBreadcrumbClick,
                                )
                            }
                        }

                        state.emptyText?.let { empty ->
                            item {
                                Text(
                                    text = empty.asString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.appColors.textSecondary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        if (state.methods.size > 1) {
                            item {
                                AppTabRow(
                                    tabs = state.methods.map { it.label },
                                    selectedIndex = state.methods.indexOfFirst { it.slug == state.selectedMethod },
                                    onTabClick = { index -> onMethodClick(state.methods[index].slug) },
                                )
                            }
                        }

                        if (state.axes.isNotEmpty()) {
                            item {
                                ConditionSelector(axes = state.axes, onConditionClick = onConditionClick)
                            }
                        }

                        state.areas.forEach { area ->
                            // Only where the location draws this method from more than one area,
                            // which is 14.5% of the time. Everywhere else the fold is invisible.
                            area.name?.let { name ->
                                item(key = "area-${area.slug}") {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.appColors.textTertiary,
                                    )
                                }
                            }

                            items(
                                count = area.rows.size,
                                key = { index -> "${area.slug}-${area.rows[index].variantSlug}" },
                            ) { index ->
                                val row = area.rows[index]
                                EncounterRow(row = row, onClick = { onPokemonClick(row.variantSlug) })
                            }
                        }
                    }
                }
            }
        }
    }
}
