package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.DetailRoute
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.DetailViewModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexRoute
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementEntry
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class PokedexNavKeyHandler(
    private val navigator: PokedexNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<DexDestination> {
            DexRoute(
                onNavigateToDetail = navigator::navigateToDetail,
                onNavigateBack = { navigator.navigateBack() },
            )
        }

        // Which Pokemon, and what its card was already showing, reach the screen through the
        // ViewModel rather than through the Route: a Route may only take lambdas, a Modifier or a
        // ViewModel. The destination survives process death, so a restored screen redraws the same
        // hero it had.
        entry<DetailDestination>(metadata = SharedElementEntry.metadata) { destination ->
            DetailRoute(
                onNavigateToMove = navigator::navigateToMove,
                onNavigateBack = { navigator.navigateBack() },
                viewModel =
                    koinViewModel<DetailViewModel>(
                        parameters = {
                            parametersOf(destination.slug, destination.hero, destination.query, destination.formSlug)
                        },
                    ),
            )
        }
    }
}
