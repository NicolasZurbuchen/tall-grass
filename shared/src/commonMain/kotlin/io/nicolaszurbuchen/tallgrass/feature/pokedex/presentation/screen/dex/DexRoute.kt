package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DexRoute(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DexViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateToDetailUpdated by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is DexLabel.NavigateToDetail -> onNavigateToDetailUpdated(label.slug)
            }
        }
    }

    DexScreen(
        state = state,
        onEntryClick = { slug -> viewModel.onIntent(DexIntent.EntryClicked(slug)) },
        onRetryClick = { viewModel.onIntent(DexIntent.RetryClicked) },
        modifier = modifier,
    )
}
