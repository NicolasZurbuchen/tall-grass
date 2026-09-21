package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.MoveDetailRoute
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.MoveDetailViewModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.MovesRoute
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementEntry
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class MovesNavKeyHandler(
    private val navigator: MovesNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<MovesListDestination> {
            MovesRoute(
                onNavigateToDetail = navigator::navigateToMoveDetail,
                onNavigateBack = { navigator.navigateBack() },
            )
        }

        // The name and the type pill travel from the card, so the host gives this the motion it
        // gives a screen reached from an element rather than a push from the side. Which move the
        // screen is about reaches it through the ViewModel rather than the Route, because a Route may
        // only take lambdas, a Modifier or a ViewModel, and the destination survives process death.
        entry<MoveDetailDestination>(metadata = SharedElementEntry.metadata) { destination ->
            MoveDetailRoute(
                onNavigateToPokemon = { label ->
                    navigator.navigateToPokemon(
                        slug = label.slug,
                        name = label.name,
                        artworkUrl = label.artworkUrl,
                        primaryTypeSlug = label.primaryTypeSlug,
                        secondaryTypeSlug = label.secondaryTypeSlug,
                    )
                },
                onNavigateBack = { navigator.navigateBack() },
                viewModel = koinViewModel<MoveDetailViewModel>(parameters = { parametersOf(destination.slug) }),
            )
        }
    }
}
