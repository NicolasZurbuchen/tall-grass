package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateBackUpdated by rememberUpdatedState(onNavigateBack)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                DetailLabel.NavigateBack -> onNavigateBackUpdated()
            }
        }
    }

    DetailScreen(
        state = state,
        onBackClick = { viewModel.onIntent(DetailIntent.BackClicked) },
        onEntrySwipe = { slug -> viewModel.onIntent(DetailIntent.EntrySelected(slug)) },
        onFormClick = { slug -> viewModel.onIntent(DetailIntent.FormSelected(slug)) },
        // The tab crosses back into the Store's own vocabulary here. The Screen may not name a State
        // type, and the Contract may not name a UiModel, so the Route is the one place that sees
        // both -- it shares a package with the Contract and needs no import to do it.
        onTabClick = { tab ->
            val selected =
                when (tab) {
                    DetailTabUiModel.ABOUT -> DetailState.Tab.ABOUT
                    DetailTabUiModel.STATS -> DetailState.Tab.STATS
                }

            viewModel.onIntent(DetailIntent.TabSelected(selected))
        },
        onRetryClick = { viewModel.onIntent(DetailIntent.RetryClicked) },
        modifier = modifier,
    )
}
