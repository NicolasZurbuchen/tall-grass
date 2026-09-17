package io.nicolaszurbuchen.tallgrass.app.navigation.impl

import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.PokedexNavigator
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.infra.navigation.AppNavigator

class PokedexNavigatorImpl(
    private val navigator: AppNavigator,
) : PokedexNavigator {
    override fun navigateToDetail(
        slug: String,
        artworkUrl: String,
        primaryTypeSlug: String,
    ) {
        navigator.navigateTo(
            DetailDestination(
                slug = slug,
                hero =
                    HeroHandoff(
                        artworkUrl = artworkUrl,
                        primaryTypeSlug = primaryTypeSlug,
                        sharedElementKey = dexArtworkKey(slug),
                    ),
            ),
        )
    }

    override fun navigateBack() {
        navigator.navigateBack()
    }
}
