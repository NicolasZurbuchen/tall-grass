package io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation

/**
 * Where a home tile can go.
 *
 * An interface rather than a direct call because a feature may not import another feature. `app/`
 * implements it, which is the one place allowed to know that both exist.
 *
 * It grows a method per destination as the features land; a tile whose method is missing is one
 * whose screen has not been built.
 */
interface HomeNavigator {
    fun navigateToPokedex()
}
