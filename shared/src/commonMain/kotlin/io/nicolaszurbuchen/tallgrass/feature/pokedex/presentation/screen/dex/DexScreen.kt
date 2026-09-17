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
                    // DECISIONS.md § The dex grid is three cards across, loaded whole
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

// DECISIONS.md § The dex grid is three cards across, loaded whole
private const val DEX_GRID_COLUMNS = 3

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

// A screenful, not the whole dex: nothing below the fold is visible and eleven hundred shimmering
// blocks would animate off-screen for no one.
private const val SKELETON_CARDS = 12
