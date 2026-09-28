package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.LocationDetailRoute
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.LocationDetailViewModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.RegionDetailRoute
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.RegionDetailViewModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.RegionsRoute
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class LocationsNavKeyHandler(
    private val navigator: LocationsNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<RegionsDestination> {
            RegionsRoute(
                onNavigateToDetail = navigator::navigateToRegionDetail,
                onNavigateBack = { navigator.navigateBack() },
            )
        }

        // Which region the screen is about reaches it through the ViewModel rather than the Route,
        // because a Route may only take lambdas, a Modifier or a ViewModel, and the destination
        // survives process death.
        entry<RegionDetailDestination> { destination ->
            RegionDetailRoute(
                onNavigateToLocation = { slug -> navigator.navigateToLocationDetail(slug) },
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
                viewModel = koinViewModel<RegionDetailViewModel>(parameters = { parametersOf(destination.slug) }),
            )
        }

        entry<LocationDetailDestination> { destination ->
            LocationDetailRoute(
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
                viewModel =
                    koinViewModel<LocationDetailViewModel>(
                        parameters = { parametersOf(destination.slug, destination.versionSlug) },
                    ),
            )
        }
    }
}
