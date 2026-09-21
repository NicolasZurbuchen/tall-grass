package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation

interface MovesNavigator {
    fun navigateToMoveDetail(slug: String)

    /**
     * Opens a Pokemon from the Learned by tab.
     *
     * Takes the hero's contents apart rather than taking a `HeroHandoff`, because that type belongs
     * to the Pokedex feature and a feature may only import from its own subtree. `app/` is the one
     * place allowed to hold both sides, so it is where these five become a destination.
     */
    fun navigateToPokemon(
        slug: String,
        name: String,
        artworkUrl: String,
        primaryTypeSlug: String,
        secondaryTypeSlug: String?,
    )

    fun navigateBack()
}
