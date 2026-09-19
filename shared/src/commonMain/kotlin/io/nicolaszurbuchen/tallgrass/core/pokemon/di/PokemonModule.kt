package io.nicolaszurbuchen.tallgrass.core.pokemon.di

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSourceImpl
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.PokemonDetailLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.PokemonDetailLocalDataSourceImpl
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.repository.PokedexRepositoryImpl
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetPokemonDetailUseCase
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val pokemonModule =
    module {
        // The data sources take the generated Queries rather than the whole database, so the types
        // they depend on live inside this slice. Handing them PokedexDatabase would reach across the
        // package boundary for the sake of one property.
        //
        // Lazily, because `get` here runs wherever the graph is first asked for -- and that is during
        // composition, on the main thread. Opening a SQLite database and, on a first launch, copying
        // a 1.2 MB asset out of the APK are not main-thread work. Deferred, both happen inside the
        // first query, which already runs off it.
        single<DexLocalDataSource> {
            DexLocalDataSourceImpl(lazy { get<PokedexDatabase>().variantQueries }, Dispatchers.Default)
        }
        single<PokemonDetailLocalDataSource> {
            PokemonDetailLocalDataSourceImpl(
                lazy { get<PokedexDatabase>().speciesQueries },
                lazy { get<PokedexDatabase>().variantQueries },
                Dispatchers.Default,
            )
        }
        singleOf(::PokedexRepositoryImpl) bind PokedexRepository::class
        singleOf(::GetDexEntriesUseCase)
        singleOf(::GetPokemonDetailUseCase)
    }
