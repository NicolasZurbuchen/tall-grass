package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MoveDetailRoute(
    onNavigateToPokemon: (MoveDetailLabel.NavigateToPokemon) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoveDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateToPokemonUpdated by rememberUpdatedState(onNavigateToPokemon)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is MoveDetailLabel.NavigateToPokemon -> onNavigateToPokemonUpdated(label)
            }
        }
    }

    MoveDetailScreen(
        state = state,
        onTabClick = { tab ->
            val selected =
                when (tab) {
                    MoveDetailTabUiModel.DETAILS -> MoveDetailState.Tab.DETAILS
                    MoveDetailTabUiModel.LEARNERS -> MoveDetailState.Tab.LEARNERS
                }

            viewModel.onIntent(MoveDetailIntent.TabSelected(selected))
        },
        onLearnerClick = { slug -> viewModel.onIntent(MoveDetailIntent.LearnerClicked(slug)) },
        onBackClick = onNavigateBack,
        onRetryClick = { viewModel.onIntent(MoveDetailIntent.RetryClicked) },
        modifier = modifier,
    )
}
