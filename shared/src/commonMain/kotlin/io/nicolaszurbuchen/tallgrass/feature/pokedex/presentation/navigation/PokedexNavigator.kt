package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

interface PokedexNavigator {
    fun navigateToDetail(
        slug: String,
        hero: HeroHandoff,
        query: DexQuery,
    )

    /**
     * Opens a move from a Pokemon's Moves tab.
     *
     * The one place this feature reaches into another, and it does so without naming it: a feature
     * may only import from its own subtree, so `app/` turns this slug into the Moves feature's
     * destination. The mirror of `MovesNavigator.navigateToPokemon`.
     */
    fun navigateToMove(slug: String)

    fun navigateToAbility(slug: String)

    fun navigateBack()
}
