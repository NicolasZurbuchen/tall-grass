package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

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
import androidx.compose.runtime.remember
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
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.component.MoveCard
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.component.MoveCardSkeleton
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.moves_title

@Composable
fun MovesScreen(
    state: MovesUiModel,
    onMoveClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        AppScreenHeader(
            title = stringResource(Res.string.moves_title),
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
                    MovesGridSkeleton()
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
                        columns = GridCells.Fixed(MOVES_GRID_COLUMNS),
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(items = state.moves, key = { _, move -> move.slug }) { index, move ->
                            MoveCard(
                                move = move,
                                onClick = { onMoveClick(move.slug) },
                                modifier = Modifier.rise(entranceFraction(index - firstOnScreen, elapsed)),
                            )
                        }
                    }
                }
            }
        }
    }
}

// The dex grid's, and for the same reason: a card wide enough for a name and a pill, in a screen that
// scrolls rather than pages.
private const val MOVES_GRID_COLUMNS = 2

@Composable
private fun MovesGridSkeleton() {
    ShimmerPulse {
        LazyVerticalGrid(
            columns = GridCells.Fixed(MOVES_GRID_COLUMNS),
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(SKELETON_CARDS) { MoveCardSkeleton() }
        }
    }
}

// A screenful, not all 919: nothing below the fold is visible, and the dex screen settled the same
// question the same way.
private const val SKELETON_CARDS = 12
