package io.nicolaszurbuchen.tallgrass.feature.pokedex.di

import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.DetailStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.DetailViewModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pokedexModule =
    module {
        factoryOf(::DexStoreFactory)
        viewModelOf(::DexViewModel)

        factoryOf(::DetailStoreFactory)

        // Parameterised rather than declared with viewModelOf: which Pokemon the screen is about and
        // what its card was showing both arrive from the NavKey, so they are passed at resolution
        // rather than resolved. The factory above stays an ordinary binding, which is what keeps its
        // own dependencies inside the graph.
        viewModel { (slug: String, hero: HeroHandoff) ->
            DetailViewModel(get(), slug, hero)
        }
    }
