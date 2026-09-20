package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

interface PokedexNavigator {
    fun navigateToDetail(
        slug: String,
        hero: HeroHandoff,
        query: DexQuery,
    )

    fun navigateBack()
}
