package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.component.AbilityCard
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.component.AbilityCardSkeleton
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.abilities_title

/**
 * All 314 abilities, alphabetically.
 *
 * **A column rather than the dex's and the moves list's two-column grid**, because the card carries a
 * sentence — see [AbilityCard]. Everything else is the same screen: the same header, the same
 * staggered entrance counted from the top of the viewport, the same skeleton while the read lands.
 *
 * No search and no filter. #65 asks for a generation filter here, on the grounds that the list is the
 * instrument for working out what a category axis should be; the author's call is that this list
 * matches the other two for now and both arrive together later.
 */
@Composable
fun AbilitiesScreen(
    state: AbilitiesUiModel,
    onAbilityClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        AppScreenHeader(
            title = stringResource(Res.string.abilities_title),
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
                    AbilitiesListSkeleton()
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
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(items = state.abilities, key = { _, ability -> ability.slug }) { index, ability ->
                            AbilityCard(
                                ability = ability,
                                onClick = { onAbilityClick(ability.slug) },
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
private fun AbilitiesListSkeleton() {
    ShimmerPulse {
        LazyColumn(
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(SKELETON_CARDS) { AbilityCardSkeleton() }
        }
    }
}

// A screenful, not all 314: nothing below the fold is visible, and the dex screen settled the same
// question the same way.
private const val SKELETON_CARDS = 8
