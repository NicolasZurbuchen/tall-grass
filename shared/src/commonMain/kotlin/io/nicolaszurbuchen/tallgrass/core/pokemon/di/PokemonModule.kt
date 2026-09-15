package io.nicolaszurbuchen.tallgrass.core.pokemon.di

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSourceImpl
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.repository.PokedexRepositoryImpl
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val pokemonModule =
    module {
        // The data source takes the generated Queries rather than the whole database, so the type
        // it depends on lives inside this slice. Handing it PokedexDatabase would reach across the
        // package boundary for the sake of one property.
        single<DexLocalDataSource> {
            DexLocalDataSourceImpl(get<PokedexDatabase>().variantQueries, Dispatchers.Default)
        }
        singleOf(::PokedexRepositoryImpl) bind PokedexRepository::class
        singleOf(::GetDexEntriesUseCase)
    }
