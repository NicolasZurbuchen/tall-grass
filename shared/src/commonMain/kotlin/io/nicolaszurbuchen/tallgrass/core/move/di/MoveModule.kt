package io.nicolaszurbuchen.tallgrass.core.move.di

import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.MoveLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.MoveLocalDataSourceImpl
import io.nicolaszurbuchen.tallgrass.core.move.data.repository.MoveRepositoryImpl
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMoveDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMoveLearnersUseCase
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMovesForVariantUseCase
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMovesUseCase
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val moveModule =
    module {
        // Lazily, and the Queries rather than the database, on the same grounds as pokemonModule.
        // DECISIONS.md § The database opens on the first query, not on the first injection
        single<MoveLocalDataSource> {
            MoveLocalDataSourceImpl(lazy { get<PokedexDatabase>().moveQueries }, Dispatchers.Default)
        }
        singleOf(::MoveRepositoryImpl) bind MoveRepository::class
        singleOf(::GetMovesUseCase)
        singleOf(::GetMoveDetailUseCase)
        singleOf(::GetMoveLearnersUseCase)
        singleOf(::GetMovesForVariantUseCase)
    }
