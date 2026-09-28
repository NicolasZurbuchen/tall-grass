package io.nicolaszurbuchen.tallgrass.feature.locations.di

import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.RegionDetailStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.RegionDetailViewModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.RegionsStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.RegionsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val locationsModule =
    module {
        factoryOf(::RegionsStoreFactory)
        viewModelOf(::RegionsViewModel)

        factoryOf(::RegionDetailStoreFactory)

        // Parameterised rather than declared with viewModelOf, on the same grounds as the ability
        // detail: which region the screen is about arrives from the NavKey, so it is passed at
        // resolution rather than resolved.
        viewModel { (slug: String) -> RegionDetailViewModel(get(), slug) }
    }
