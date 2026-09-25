package io.nicolaszurbuchen.tallgrass.app.navigation.impl

import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation.AbilitiesNavigator
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation.AbilityDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.infra.navigation.AppNavigator

class AbilitiesNavigatorImpl(
    private val navigator: AppNavigator,
) : AbilitiesNavigator {
    override fun navigateToAbilityDetail(slug: String) {
        navigator.navigateTo(AbilityDetailDestination(slug))
    }

    /**
     * **One of the two places the Abilities and Pokedex features meet**, and the reason this is in
     * `app/`: it names one feature's vocabulary on one side and the other's on the other, and neither
     * of them is allowed to name the other. The move detail's Learned by tab does the same thing.
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
                // The whole dex, not the holders. An ability's holders are not a dex query the
                // carousel can walk, so the detail opens on the dex it belongs to rather than with
                // neighbours that would be the wrong ones.
                query = DexQuery.All,
                hero =
                    HeroHandoff(
                        name = name,
                        artworkUrl = artworkUrl,
                        primaryTypeSlug = primaryTypeSlug,
                        secondaryTypeSlug = secondaryTypeSlug,
                        // Keyed on the form, because the form is what the hero draws. Nothing sends
                        // on it here -- a holder card is not registered as a shared element -- so the
                        // transition cross-fades, which is the same path a back-navigation into the
                        // dex already takes.
                        // DECISIONS.md § The artwork flies out of the grid and does not fly back
                        sharedElementKey = dexArtworkKey(formSlug),
                    ),
            ),
        )
    }

    override fun navigateBack() {
        navigator.navigateBack()
    }
}
