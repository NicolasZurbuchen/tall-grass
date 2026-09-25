package io.nicolaszurbuchen.tallgrass.app.di

import io.nicolaszurbuchen.tallgrass.app.navigation.appNavigationModule
import io.nicolaszurbuchen.tallgrass.core.ability.di.abilityModule
import io.nicolaszurbuchen.tallgrass.core.move.di.moveModule
import io.nicolaszurbuchen.tallgrass.core.pokemon.di.pokemonModule
import io.nicolaszurbuchen.tallgrass.core.type.di.typeModule
import io.nicolaszurbuchen.tallgrass.feature.abilities.di.abilitiesModule
import io.nicolaszurbuchen.tallgrass.feature.moves.di.movesModule
import io.nicolaszurbuchen.tallgrass.feature.pokedex.di.pokedexModule
import io.nicolaszurbuchen.tallgrass.infra.database.databaseModule
import io.nicolaszurbuchen.tallgrass.infra.image.imageModule
import io.nicolaszurbuchen.tallgrass.infra.mvi.storeModule
import io.nicolaszurbuchen.tallgrass.infra.navigation.infraNavigationModule
import io.nicolaszurbuchen.tallgrass.infra.network.networkModule

val appModule =
    listOf(
        appNavigationModule,
        databaseModule,
        imageModule,
        infraNavigationModule,
        networkModule,
        storeModule,
        abilityModule,
        moveModule,
        pokemonModule,
        typeModule,
        abilitiesModule,
        movesModule,
        pokedexModule,
    )
