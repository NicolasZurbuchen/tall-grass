package io.nicolaszurbuchen.tallgrass.core.pokemon.data.repository

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository

class PokedexRepositoryImpl(
    private val localDataSource: DexLocalDataSource,
) : PokedexRepository {
    override suspend fun dexEntries(): List<DexEntry> = localDataSource.dexEntries()
}
