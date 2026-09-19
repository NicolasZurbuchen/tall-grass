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
        // Lazily: `get` runs on whichever thread first asks the graph for this, and that is the main
        // one, during composition.
        // DECISIONS.md § The database opens on the first query, not on the first injection
        single<TypeLocalDataSource> {
            TypeLocalDataSourceImpl(lazy { get<PokedexDatabase>().typeQueries }, Dispatchers.Default)
        }
        singleOf(::TypeRepositoryImpl) bind TypeRepository::class
        singleOf(::GetTypeMatchupsUseCase)
    }
