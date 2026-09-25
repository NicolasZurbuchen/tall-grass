package io.nicolaszurbuchen.tallgrass.core.ability.di

import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.AbilityLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.AbilityLocalDataSourceImpl
import io.nicolaszurbuchen.tallgrass.core.ability.data.repository.AbilityRepositoryImpl
import io.nicolaszurbuchen.tallgrass.core.ability.domain.repository.AbilityRepository
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilitiesForVariantUseCase
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilitiesUseCase
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilityDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilityHoldersUseCase
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val abilityModule =
    module {
        // Lazily, and the Queries rather than the database, on the same grounds as pokemonModule.
        // DECISIONS.md § The database opens on the first query, not on the first injection
        single<AbilityLocalDataSource> {
            AbilityLocalDataSourceImpl(lazy { get<PokedexDatabase>().abilityQueries }, Dispatchers.Default)
        }
        singleOf(::AbilityRepositoryImpl) bind AbilityRepository::class
        singleOf(::GetAbilitiesUseCase)
        singleOf(::GetAbilityDetailUseCase)
        singleOf(::GetAbilityHoldersUseCase)
        singleOf(::GetAbilitiesForVariantUseCase)
    }
