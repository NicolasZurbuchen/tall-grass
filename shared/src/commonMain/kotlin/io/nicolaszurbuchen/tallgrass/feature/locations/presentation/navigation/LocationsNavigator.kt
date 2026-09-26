package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation

interface LocationsNavigator {
    fun navigateToRegionDetail(slug: String)

    /**
     * Opens a Pokemon from a region's Pokedex tab.
     *
     * Takes the card's contents apart rather than taking a shared handoff type, on the same grounds
     * as `AbilitiesNavigator`: that type belongs to the Pokedex feature, and a feature may only
     * import from its own subtree. `app/` is the one place allowed to hold both sides.
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
