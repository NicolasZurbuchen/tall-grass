package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.fake

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository

class FakePokedexRepository(
    private var entries: List<DexEntry> = emptyList(),
    private var failure: Throwable? = null,
) : PokedexRepository {
    var callCount: Int = 0
        private set

    override suspend fun dexEntries(): List<DexEntry> {
        callCount++
        failure?.let { throw it }
        return entries
    }
}
