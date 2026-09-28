package io.nicolaszurbuchen.tallgrass.app.navigation.impl

import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation.AbilityDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation.LocationDetailDestination
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

    /**
     * The Moves tab reaches two other features, and `app/` is the only place allowed to name both
     * sides. Same shape as the move above it.
     */
    override fun navigateToAbility(slug: String) {
        navigator.navigateTo(AbilityDetailDestination(slug))
    }

    /**
     * **Where the Pokedex and Locations features meet**, and a third reason this lives in `app/`: it
     * names one feature's vocabulary on one side and the other's on the other. The route side's
     * mirror is `LocationsNavigator.navigateToPokemon`.
     *
     * [versionSlug] is what closes #24's loop. The reader arrived from a cell they had already
     * chosen, so the route opens on that game rather than asking the question a second time.
     */
    override fun navigateToLocation(
        slug: String,
        versionSlug: String,
    ) {
        navigator.navigateTo(LocationDetailDestination(slug, versionSlug))
    }

    override fun navigateBack() {
        navigator.navigateBack()
    }
}
