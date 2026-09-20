package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppScreenHeader
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.rememberEntranceClock
import io.nicolaszurbuchen.tallgrass.design.theme.rememberReducedMotion
import io.nicolaszurbuchen.tallgrass.design.theme.rise
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component.DexCard
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component.DexCardSkeleton
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component.PrefetchBanner
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_dex_title

@Composable
fun DexScreen(
    state: DexUiModel,
    onEntryClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        AppScreenHeader(
            title = stringResource(Res.string.pokedex_dex_title),
            onBackClick = onBackClick,
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
                    DexGridSkeleton()
                }

                else -> {
                    val gridState = rememberLazyGridState()
                    val elapsed by rememberEntranceClock(enabled = !rememberReducedMotion())

                    // Captured once, not read every frame. The stagger counts from the top of the
                    // *viewport*, and scrolling during the entrance would otherwise keep moving the
                    // row the count starts from.
                    val firstOnScreen = remember { gridState.firstVisibleItemIndex }

                    // At most one card is ever a shared element: the one that was tapped. Saved
                    // rather than remembered for the same reason the entrance flag is -- the host
                    // disposes this composition while the detail is open, and the way back needs the
                    // sending half still here to match against.
                    var heroSlug by rememberSaveable { mutableStateOf<String?>(null) }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(DEX_GRID_COLUMNS),
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(items = state.entries, key = { _, entry -> entry.slug }) { index, entry ->
                            DexCard(
                                entry = entry,
                                isHero = entry.slug == heroSlug,
                                onClick = {
                                    heroSlug = entry.slug
                                    onEntryClick(entry.slug)
                                },
                                modifier = Modifier.rise(entranceFraction(index - firstOnScreen, elapsed)),
                            )
                        }
                    }
                }
            }

            // At the foot of the grid rather than in it, so it neither scrolls away nor takes a row
            // from the cards. It is the only thing on this screen that is about the app rather than
            // about Pokemon, and it leaves as soon as the run does.
            state.prefetch?.let { prefetch ->
                PrefetchBanner(prefetch = prefetch, modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

// DECISIONS.md § The dex grid is two cards across, loaded whole
private const val DEX_GRID_COLUMNS = 2

@Composable
private fun DexGridSkeleton() {
    ShimmerPulse {
        LazyVerticalGrid(
            columns = GridCells.Fixed(DEX_GRID_COLUMNS),
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(SKELETON_CARDS) { DexCardSkeleton() }
        }
    }
}

// A screenful, not the whole dex: nothing below the fold is visible and a thousand shimmering
// blocks would animate off-screen for no one.
private const val SKELETON_CARDS = 12
