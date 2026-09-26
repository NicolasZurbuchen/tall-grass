package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegionsRoute(
    onNavigateToDetail: (slug: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateToDetailUpdated by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is RegionsLabel.NavigateToDetail -> onNavigateToDetailUpdated(label.slug)
            }
        }
    }

    RegionsScreen(
        state = state,
        onRegionClick = { slug -> viewModel.onIntent(RegionsIntent.RegionClicked(slug)) },
        onBackClick = onNavigateBack,
        onRetryClick = { viewModel.onIntent(RegionsIntent.RetryClicked) },
        modifier = modifier,
    )
}
