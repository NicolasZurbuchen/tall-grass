package io.nicolaszurbuchen.tallgrass.feature.pokedex.di

import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.DexViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pokedexModule =
    module {
        factoryOf(::DexStoreFactory)
        viewModelOf(::DexViewModel)
    }
