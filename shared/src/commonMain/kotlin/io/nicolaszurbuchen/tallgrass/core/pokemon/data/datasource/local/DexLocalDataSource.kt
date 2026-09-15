package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

interface DexLocalDataSource {
    suspend fun dexEntries(): List<DexEntry>
}
