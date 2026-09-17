package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexRoute
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler

class PokedexNavKeyHandler : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        // Pokemon detail is #33's, so tapping a card does nothing yet. The callback stays rather
        // than the screen being written without one: the slug it carries is what the detail route
        // is keyed by, and the shared-element transition needs the tap to already be wired.
        entry<DexDestination> {
            DexRoute(onNavigateToDetail = { })
        }
    }
}
