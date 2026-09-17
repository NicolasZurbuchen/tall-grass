package io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation

/**
 * Where a home tile can go.
 *
 * Gains a method per destination as the features land; a tile whose method is missing is one whose
 * screen does not exist yet.
 */
interface HomeNavigator {
    fun navigateToPokedex()
}
