package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MovesRoute(
    onNavigateToDetail: (slug: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateToDetailUpdated by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is MovesLabel.NavigateToDetail -> onNavigateToDetailUpdated(label.slug)
            }
        }
    }

    MovesScreen(
        state = state,
        onMoveClick = { slug -> viewModel.onIntent(MovesIntent.MoveClicked(slug)) },
        onBackClick = onNavigateBack,
        onRetryClick = { viewModel.onIntent(MovesIntent.RetryClicked) },
        modifier = modifier,
    )
}
