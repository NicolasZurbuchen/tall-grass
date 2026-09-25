package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation

interface AbilitiesNavigator {
    fun navigateToAbilityDetail(slug: String)

    /**
     * Opens a Pokemon from the Known by tab.
     *
     * Takes the hero's contents apart rather than taking a `HeroHandoff`, because that type belongs
     * to the Pokedex feature and a feature may only import from its own subtree. `app/` is the one
     * place allowed to hold both sides, so it is where these six become a destination.
     */
    fun navigateToPokemon(
        cardSlug: String,
        formSlug: String,
        name: String,
        artworkUrl: String,
        primaryTypeSlug: String,
        secondaryTypeSlug: String?,
    )

    fun navigateBack()
}
