package io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.HomeRoute
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.uimodel.HomeTileUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler

class HomeNavKeyHandler(
    private val navigator: HomeNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<HomeRootDestination> {
            HomeRoute(
                onTileClick = { tile ->
                    when (tile) {
                        HomeTileUiModel.POKEDEX -> navigator.navigateToPokedex()

                        HomeTileUiModel.MOVES -> navigator.navigateToMoves()

                        HomeTileUiModel.ABILITIES -> navigator.navigateToAbilities()

                        HomeTileUiModel.REGIONS -> navigator.navigateToRegions()

                        // Every other feature is still a ticket. The tiles render and do nothing,
                        // which is better than hiding them: the home screen is the app's table of
                        // contents and a shorter one would misrepresent what is coming.
                        HomeTileUiModel.ITEMS,
                        HomeTileUiModel.REGIONS,
                        HomeTileUiModel.TYPE_CHART,
                        HomeTileUiModel.TEAM_BUILDER,
                        HomeTileUiModel.COMPARE,
                        -> Unit
                    }
                },
            )
        }
    }
}
