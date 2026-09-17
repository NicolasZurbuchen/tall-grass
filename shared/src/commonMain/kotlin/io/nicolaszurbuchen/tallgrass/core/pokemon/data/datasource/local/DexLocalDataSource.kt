package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

interface DexLocalDataSource {
    /** Every variant that earns a grid card, in National Dex order with a species' forms adjacent. */
    suspend fun dexEntries(): List<DexEntry>
}
