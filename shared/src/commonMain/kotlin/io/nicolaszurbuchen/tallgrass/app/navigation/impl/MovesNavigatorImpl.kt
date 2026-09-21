package io.nicolaszurbuchen.tallgrass.app.navigation.impl

import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.MoveDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.MovesNavigator
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.infra.navigation.AppNavigator

class MovesNavigatorImpl(
    private val navigator: AppNavigator,
) : MovesNavigator {
    override fun navigateToMoveDetail(slug: String) {
        navigator.navigateTo(MoveDetailDestination(slug))
    }

    /**
     * **The one place the two features meet**, and the reason this is in `app/`: it names the Moves
     * feature's vocabulary on one side and the Pokedex feature's on the other, and neither of them is
     * allowed to name the other.
     */
    override fun navigateToPokemon(
        slug: String,
        name: String,
        artworkUrl: String,
        primaryTypeSlug: String,
        secondaryTypeSlug: String?,
    ) {
        navigator.navigateTo(
            DetailDestination(
                slug = slug,
                // The list that was open, not the whole dex. A move's learners are not a dex query
                // the carousel can walk, so the detail opens without its neighbours rather than with
                // the wrong ones.
                query = DexQuery.All,
                hero =
                    HeroHandoff(
                        name = name,
                        artworkUrl = artworkUrl,
                        primaryTypeSlug = primaryTypeSlug,
                        secondaryTypeSlug = secondaryTypeSlug,
                        // The key the hero receives on. Nothing sends on it here -- a learner card is
                        // not registered as a shared element -- and the transition cross-fades, which
                        // is the same path a back-navigation into the dex already takes.
                        // DECISIONS.md § The artwork flies out of the grid and does not fly back
                        sharedElementKey = dexArtworkKey(slug),
                    ),
            ),
        )
    }

    override fun navigateBack() {
        navigator.navigateBack()
    }
}
