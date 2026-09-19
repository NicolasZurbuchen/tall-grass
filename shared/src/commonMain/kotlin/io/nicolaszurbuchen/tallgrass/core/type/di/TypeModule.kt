package io.nicolaszurbuchen.tallgrass.core.type.di

import io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.TypeLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.TypeLocalDataSourceImpl
import io.nicolaszurbuchen.tallgrass.core.type.data.repository.TypeRepositoryImpl
import io.nicolaszurbuchen.tallgrass.core.type.domain.repository.TypeRepository
import io.nicolaszurbuchen.tallgrass.core.type.domain.usecase.GetTypeMatchupsUseCase
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val typeModule =
    module {
        // Lazily, for the reason spelled out in PokemonModule: `get` runs on whichever thread first
        // asks the graph for this, and that is the main one.
        single<TypeLocalDataSource> {
            TypeLocalDataSourceImpl(lazy { get<PokedexDatabase>().typeQueries }, Dispatchers.Default)
        }
        singleOf(::TypeRepositoryImpl) bind TypeRepository::class
        singleOf(::GetTypeMatchupsUseCase)
    }
