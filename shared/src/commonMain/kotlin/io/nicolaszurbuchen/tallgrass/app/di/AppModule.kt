package io.nicolaszurbuchen.tallgrass.app.di

import io.nicolaszurbuchen.tallgrass.app.navigation.appNavigationModule
import io.nicolaszurbuchen.tallgrass.core.pokemon.di.pokemonModule
import io.nicolaszurbuchen.tallgrass.feature.pokedex.di.pokedexModule
import io.nicolaszurbuchen.tallgrass.infra.database.databaseModule
import io.nicolaszurbuchen.tallgrass.infra.mvi.storeModule
import io.nicolaszurbuchen.tallgrass.infra.navigation.infraNavigationModule
import io.nicolaszurbuchen.tallgrass.infra.network.networkModule

val appModule =
    listOf(
        appNavigationModule,
        databaseModule,
        infraNavigationModule,
        networkModule,
        storeModule,
        pokemonModule,
        pokedexModule,
    )
