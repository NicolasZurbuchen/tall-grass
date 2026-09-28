package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LocationDetailRoute(
    onNavigateToPokemon: (LocationDetailLabel.NavigateToPokemon) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onPokemon by rememberUpdatedState(onNavigateToPokemon)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is LocationDetailLabel.NavigateToPokemon -> onPokemon(label)
            }
        }
    }

    LocationDetailScreen(
        state = state,
        onVersionClick = { slug -> viewModel.onIntent(LocationDetailIntent.VersionSelected(slug)) },
        onBreadcrumbClick = { viewModel.onIntent(LocationDetailIntent.VersionCleared) },
        onMethodClick = { method -> viewModel.onIntent(LocationDetailIntent.MethodSelected(method)) },
        onConditionClick = { axis, value -> viewModel.onIntent(LocationDetailIntent.ConditionSelected(axis, value)) },
        onPokemonClick = { slug -> viewModel.onIntent(LocationDetailIntent.PokemonClicked(slug)) },
        onBackClick = onNavigateBack,
        onRetryClick = { viewModel.onIntent(LocationDetailIntent.RetryClicked) },
        modifier = modifier,
    )
}
