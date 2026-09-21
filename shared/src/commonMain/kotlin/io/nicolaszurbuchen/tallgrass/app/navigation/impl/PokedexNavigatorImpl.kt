package io.nicolaszurbuchen.tallgrass.app.navigation.impl

import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.MoveDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.PokedexNavigator
import io.nicolaszurbuchen.tallgrass.infra.navigation.AppNavigator

class PokedexNavigatorImpl(
    private val navigator: AppNavigator,
) : PokedexNavigator {
    override fun navigateToDetail(
        slug: String,
        hero: HeroHandoff,
        query: DexQuery,
    ) {
        navigator.navigateTo(DetailDestination(slug = slug, hero = hero, query = query))
    }

    override fun navigateToMove(slug: String) {
        navigator.navigateTo(MoveDetailDestination(slug))
    }

    override fun navigateBack() {
        navigator.navigateBack()
    }
}
