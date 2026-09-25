package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.AbilitiesRoute
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.AbilityDetailRoute
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.AbilityDetailViewModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class AbilitiesNavKeyHandler(
    private val navigator: AbilitiesNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<AbilitiesListDestination> {
            AbilitiesRoute(
                onNavigateToDetail = navigator::navigateToAbilityDetail,
                onNavigateBack = { navigator.navigateBack() },
            )
        }

        // No SharedElementEntry metadata, unlike the move detail: nothing travels into this screen,
        // so the host gives it the push it gives any other screen. See `AbilityDetailDestination`.
        // Which ability the screen is about reaches it through the ViewModel rather than the Route,
        // because a Route may only take lambdas, a Modifier or a ViewModel, and the destination
        // survives process death.
        entry<AbilityDetailDestination> { destination ->
            AbilityDetailRoute(
                onNavigateToPokemon = { label ->
                    navigator.navigateToPokemon(
                        cardSlug = label.cardSlug,
                        formSlug = label.formSlug,
                        name = label.name,
                        artworkUrl = label.artworkUrl,
                        primaryTypeSlug = label.primaryTypeSlug,
                        secondaryTypeSlug = label.secondaryTypeSlug,
                    )
                },
                onNavigateBack = { navigator.navigateBack() },
                viewModel = koinViewModel<AbilityDetailViewModel>(parameters = { parametersOf(destination.slug) }),
            )
        }
    }
}
