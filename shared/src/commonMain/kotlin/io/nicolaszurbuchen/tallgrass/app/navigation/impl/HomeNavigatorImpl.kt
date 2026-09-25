package io.nicolaszurbuchen.tallgrass.app.navigation.impl

import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation.AbilitiesListDestination
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation.HomeNavigator
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.MovesListDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexDestination
import io.nicolaszurbuchen.tallgrass.infra.navigation.AppNavigator

class HomeNavigatorImpl(
    private val navigator: AppNavigator,
) : HomeNavigator {
    override fun navigateToPokedex() {
        navigator.navigateTo(DexDestination)
    }

    override fun navigateToMoves() {
        navigator.navigateTo(MovesListDestination)
    }

    override fun navigateToAbilities() {
        navigator.navigateTo(AbilitiesListDestination)
    }
}
