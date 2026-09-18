package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

interface PokedexNavigator {
    fun navigateToDetail(
        slug: String,
        artworkUrl: String,
        primaryTypeSlug: String,
    )

    fun navigateBack()
}
