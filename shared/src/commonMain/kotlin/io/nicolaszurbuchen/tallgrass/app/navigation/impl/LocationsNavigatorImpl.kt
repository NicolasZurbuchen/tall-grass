package io.nicolaszurbuchen.tallgrass.app.navigation.impl

import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation.LocationsNavigator
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation.RegionDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.infra.navigation.AppNavigator

class LocationsNavigatorImpl(
    private val navigator: AppNavigator,
) : LocationsNavigator {
    override fun navigateToRegionDetail(slug: String) {
        navigator.navigateTo(RegionDetailDestination(slug))
    }

    /**
     * **Where the Locations and Pokedex features meet**, and the reason this lives in `app/`: it
     * names one feature's vocabulary on one side and the other's on the other, and neither is allowed
     * to name the other. The abilities and moves navigators do the same thing.
     */
    override fun navigateToPokemon(
        cardSlug: String,
        formSlug: String,
        name: String,
        artworkUrl: String,
        primaryTypeSlug: String,
        secondaryTypeSlug: String?,
    ) {
        navigator.navigateTo(
            DetailDestination(
                slug = cardSlug,
                formSlug = formSlug,
                // The whole dex rather than the region's. A regional Pokedex is not a query the
                // detail's carousel can walk, so it opens on the dex it belongs to rather than with
                // neighbours that would be the wrong ones -- the same call the ability detail made.
                query = DexQuery.All,
                hero =
                    HeroHandoff(
                        name = name,
                        artworkUrl = artworkUrl,
                        primaryTypeSlug = primaryTypeSlug,
                        secondaryTypeSlug = secondaryTypeSlug,
                        // Keyed on the form, because the form is what the hero draws. Nothing sends
                        // on it here -- a region dex card is not registered as a shared element --
                        // so the transition cross-fades.
                        sharedElementKey = dexArtworkKey(formSlug),
                    ),
            ),
        )
    }

    override fun navigateBack() {
        navigator.navigateBack()
    }
}
