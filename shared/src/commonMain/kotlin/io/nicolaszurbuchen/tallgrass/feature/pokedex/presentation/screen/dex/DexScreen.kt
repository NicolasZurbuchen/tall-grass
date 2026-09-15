package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component.DexCard
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component.DexCardSkeleton

@Composable
fun DexScreen(
    state: DexUiModel,
    onEntryClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(DEX_GRID_COLUMNS),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(MaterialTheme.spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    // Keyed by slug so the grid keeps its scroll position and recycles correctly.
                    // The Dex number would not do: Vulpix and Alolan Vulpix are both #037.
                    items(items = state.entries, key = { it.slug }) { entry ->
                        DexCard(
                            name = entry.name,
                            numberText = entry.numberText,
                            formLabel = entry.formLabel,
                            artworkUrl = entry.artworkUrl,
                            tint = entry.tint,
                            onClick = { onEntryClick(entry.slug) },
                        )
                    }
                }
            }
        }
    }
}

// Three across is what makes roughly eleven hundred cards feel like a reference rather than a list:
// the artwork stays large enough to recognise at a glance and a generation is a few flicks apart.
private const val DEX_GRID_COLUMNS = 3

/**
 * The grid waiting as its own silhouette.
 *
 * A screenful of cards rather than the whole dex: nothing below the fold is visible, and eleven
 * hundred shimmering blocks would animate off-screen for no one.
 */
@Composable
private fun DexGridSkeleton() {
    ShimmerPulse {
        LazyVerticalGrid(
            columns = GridCells.Fixed(DEX_GRID_COLUMNS),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            userScrollEnabled = false,
        ) {
            items(SKELETON_CARDS) { DexCardSkeleton() }
        }
    }
}

private const val SKELETON_CARDS = 12
