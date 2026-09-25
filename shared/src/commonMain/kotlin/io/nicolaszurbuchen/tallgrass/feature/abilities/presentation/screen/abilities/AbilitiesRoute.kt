package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AbilitiesRoute(
    onNavigateToDetail: (slug: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AbilitiesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateToDetailUpdated by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is AbilitiesLabel.NavigateToDetail -> onNavigateToDetailUpdated(label.slug)
            }
        }
    }

    AbilitiesScreen(
        state = state,
        onAbilityClick = { slug -> viewModel.onIntent(AbilitiesIntent.AbilityClicked(slug)) },
        onBackClick = onNavigateBack,
        onRetryClick = { viewModel.onIntent(AbilitiesIntent.RetryClicked) },
        modifier = modifier,
    )
}
