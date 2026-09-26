package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
 * The eleven regions, in release order with the spin-off last.
 *
 * **A single column of wide cards rather than the dex's two-column grid.** A region card is a box
 * with a pair of mascots falling off its corner and four lines of text beside them, and at half the
 * width the artwork wins and the words stop being readable. Eleven rows is also short enough to be
 * scanned rather than scrolled, which a grid would not improve.
 *
 * No search. Eleven is barely more than a screenful, and a field over it would be furniture -- the
 * Locations tab inside a region is where the list gets long enough to need one.
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
                    val listState = rememberLazyListState()
                    val elapsed by rememberEntranceClock(enabled = !rememberReducedMotion())

                    // Captured once, not read every frame: the stagger counts from the top of the
                    // viewport, and scrolling during the entrance would keep moving the row it
                    // counts from. Same reasoning as the dex grid's.
                    val firstOnScreen = remember { listState.firstVisibleItemIndex }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
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
        LazyColumn(
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(SKELETON_CARDS) { RegionCardSkeleton() }
        }
    }
}

// A screenful, not all eleven: nothing below the fold is visible, and the other two lists settled the
// same question the same way.
private const val SKELETON_CARDS = 4
