package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DexLocalDataSourceImpl(
    private val queries: VariantQueries,
    private val dispatcher: CoroutineDispatcher,
) : DexLocalDataSource {
    /**
     * Reads the whole grid in one query.
     *
     * Roughly eleven hundred rows, read once and held by the store rather than paged. Paging would
     * buy nothing here: the dataset is on the device, the rows are small, and a Pokedex that cannot
     * be scrolled to the end without a round trip is worse than one costing a few hundred kilobytes
     * of heap.
     *
     * The dispatcher is injected rather than hardcoded so a test can read on its own scheduler.
     */
    override suspend fun dexEntries(): List<DexEntry> =
        withContext(dispatcher) {
            queries
                .selectDexEntries()
                .executeAsList()
                .mapNotNull { it.toDomain() }
        }
}
