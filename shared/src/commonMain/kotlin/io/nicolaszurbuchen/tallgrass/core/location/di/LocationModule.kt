package io.nicolaszurbuchen.tallgrass.core.location.di

import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.LocationLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.LocationLocalDataSourceImpl
import io.nicolaszurbuchen.tallgrass.core.location.data.repository.LocationRepositoryImpl
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetEncounterConditionsUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetLocationDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetLocationEncountersUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionDexUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionLocationsUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionsUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetVariantAvailabilityUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetVariantEncountersUseCase
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val locationModule =
    module {
        // Lazily, and the Queries rather than the database, on the same grounds as pokemonModule.
        // DECISIONS.md § The database opens on the first query, not on the first injection
        single<LocationLocalDataSource> {
            LocationLocalDataSourceImpl(
                lazy { get<PokedexDatabase>().regionQueries },
                lazy { get<PokedexDatabase>().locationQueries },
                lazy { get<PokedexDatabase>().encounterQueries },
                Dispatchers.Default,
            )
        }
        singleOf(::LocationRepositoryImpl) bind LocationRepository::class
        singleOf(::GetRegionsUseCase)
        singleOf(::GetRegionDetailUseCase)
        singleOf(::GetRegionLocationsUseCase)
        singleOf(::GetRegionDexUseCase)
        singleOf(::GetLocationDetailUseCase)
        singleOf(::GetLocationEncountersUseCase)
        singleOf(::GetVariantEncountersUseCase)
        singleOf(::GetVariantAvailabilityUseCase)
        singleOf(::GetEncounterConditionsUseCase)
    }
