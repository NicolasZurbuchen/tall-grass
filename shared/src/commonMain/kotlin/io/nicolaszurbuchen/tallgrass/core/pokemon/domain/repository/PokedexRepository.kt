package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

interface PokedexRepository {
    suspend fun dexEntries(): List<DexEntry>
}
