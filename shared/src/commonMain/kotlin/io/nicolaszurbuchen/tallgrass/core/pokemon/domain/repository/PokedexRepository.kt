package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

interface PokedexRepository {
    /** Every variant that earns a grid card, in National Dex order with a species' forms adjacent. */
    suspend fun dexEntries(): List<DexEntry>
}
