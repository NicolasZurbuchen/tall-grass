package io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.HomeRoute
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler

class HomeNavKeyHandler : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        // Every tile's destination belongs to a feature that does not exist yet, so the callback is
        // deliberately empty rather than absent: the seam is what later tickets attach a navigator
        // to, and adding it now keeps them from reshaping this screen to get one.
        entry<HomeRootDestination> {
            HomeRoute(onTileClick = { })
        }
    }
}
