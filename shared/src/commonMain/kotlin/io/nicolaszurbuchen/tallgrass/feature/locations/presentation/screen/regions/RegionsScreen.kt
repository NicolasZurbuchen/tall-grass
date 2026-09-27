package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppScreenHeader
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.rememberEntranceClock
import io.nicolaszurbuchen.tallgrass.design.theme.rememberReducedMotion
import io.nicolaszurbuchen.tallgrass.design.theme.rise
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.component.RegionCard
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.component.RegionCardSkeleton
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.regions_subtitle
import tallgrass.shared.generated.resources.regions_title

/**
 * The ten main-series regions, in release order.
 *
 * **Two columns, the same as the dex grid.** One wide card per row was too big: ten of them needed
 * scrolling for no reason, and the mascots ended up larger than the words. Halved, a card drops the
 * Japanese name and keeps the pair inset rather than bled off the corner -- see `RegionCard`.
 *
 * Orre is not here. It exists only in Colosseum and XD, which are spin-offs, and this app covers the
 * main series.
 *
 * No search. Ten cards is one screenful, and a field over it would be furniture.
 */
@Composable
fun RegionsScreen(
    state: RegionsUiModel,
    onRegionClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        AppScreenHeader(
            title = stringResource(Res.string.regions_title),
            onBackClick = onBackClick,
        )

        Text(
            text = stringResource(Res.string.regions_subtitle),
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
                    RegionsListSkeleton()
                }

                else -> {
                    val gridState = rememberLazyGridState()
                    val elapsed by rememberEntranceClock(enabled = !rememberReducedMotion())

                    // Captured once, not read every frame: the stagger counts from the top of the
                    // viewport, and scrolling during the entrance would keep moving the row it
                    // counts from. Same reasoning as the dex grid's.
                    val firstOnScreen = remember { gridState.firstVisibleItemIndex }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(GRID_COLUMNS),
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(items = state.regions, key = { _, region -> region.slug }) { index, region ->
                            RegionCard(
                                region = region,
                                onClick = { onRegionClick(region.slug) },
                                modifier = Modifier.rise(entranceFraction(index - firstOnScreen, elapsed)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionsListSkeleton() {
    ShimmerPulse {
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(SKELETON_CARDS) { RegionCardSkeleton() }
        }
    }
}

// A screenful, not all ten: nothing below the fold is visible, and the other two lists settled the
// same question the same way. Six rather than four now that they come two to a row.
private const val SKELETON_CARDS = 6

// Two columns, which is what the ten cards want: at full width a region card left the artwork much
// larger than the words and a list of ten needed scrolling for no reason.
private const val GRID_COLUMNS = 2
