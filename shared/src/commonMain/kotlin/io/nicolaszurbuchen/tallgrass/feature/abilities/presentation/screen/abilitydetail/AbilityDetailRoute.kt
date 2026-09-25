package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityDetailTabUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AbilityDetailRoute(
    onNavigateToPokemon: (AbilityDetailLabel.NavigateToPokemon) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AbilityDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateToPokemonUpdated by rememberUpdatedState(onNavigateToPokemon)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is AbilityDetailLabel.NavigateToPokemon -> onNavigateToPokemonUpdated(label)
            }
        }
    }

    AbilityDetailScreen(
        state = state,
        onTabClick = { tab ->
            val selected =
                when (tab) {
                    AbilityDetailTabUiModel.DETAILS -> AbilityDetailState.Tab.DETAILS
                    AbilityDetailTabUiModel.HOLDERS -> AbilityDetailState.Tab.HOLDERS
                }

            viewModel.onIntent(AbilityDetailIntent.TabSelected(selected))
        },
        onHolderClick = { slug -> viewModel.onIntent(AbilityDetailIntent.HolderClicked(slug)) },
        onBackClick = onNavigateBack,
        onRetryClick = { viewModel.onIntent(AbilityDetailIntent.RetryClicked) },
        modifier = modifier,
    )
}
