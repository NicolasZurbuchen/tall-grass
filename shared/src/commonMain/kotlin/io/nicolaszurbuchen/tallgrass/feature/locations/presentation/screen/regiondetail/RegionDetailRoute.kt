package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegionDetailRoute(
    onNavigateToPokemon: (RegionDetailLabel.NavigateToPokemon) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegionDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onPokemon by rememberUpdatedState(onNavigateToPokemon)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is RegionDetailLabel.NavigateToPokemon -> onPokemon(label)
            }
        }
    }

    RegionDetailScreen(
        state = state,
        onTabClick = { index -> viewModel.onIntent(RegionDetailIntent.TabSelected(index)) },
        onQueryChange = { query -> viewModel.onIntent(RegionDetailIntent.QueryChanged(query)) },
        onPokemonClick = { slug -> viewModel.onIntent(RegionDetailIntent.PokemonClicked(slug)) },
        onBackClick = onNavigateBack,
        onRetryClick = { viewModel.onIntent(RegionDetailIntent.RetryClicked) },
        modifier = modifier,
    )
}
