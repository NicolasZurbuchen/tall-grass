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

    /**
     * Opens a route from a Pokemon's Location tab, on the game it was found in.
     *
     * The cross-link #24 asks for, and the second place this feature reaches into another without
     * naming it. [versionSlug] is what closes the loop: the reader arrived from a cell they had
     * already chosen, and landing them back at the grid would ask the question twice.
     */
    fun navigateToLocation(
        slug: String,
        versionSlug: String,
    )

    fun navigateBack()
}
