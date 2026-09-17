package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DexLocalDataSourceImpl(
    private val queries: VariantQueries,
    private val dispatcher: CoroutineDispatcher,
) : DexLocalDataSource {
    override suspend fun dexEntries(): List<DexEntry> =
        withContext(dispatcher) {
            queries
                .selectDexEntries()
                .executeAsList()
                .mapNotNull { it.toDomain() }
        }
}
