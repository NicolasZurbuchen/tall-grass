package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.fake

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository

/**
 * Counted per query rather than in total: the detail screen reads both — the Pokemon it is about and
 * the list its carousel walks — so one counter could not tell a retry from a neighbour.
 */
class FakePokedexRepository(
    private var entries: List<DexEntry> = emptyList(),
    private var details: Map<String, PokemonDetail> = emptyMap(),
    private var failure: Throwable? = null,
) : PokedexRepository {
    var entriesCallCount: Int = 0
        private set

    var detailCallCount: Int = 0
        private set

    override suspend fun dexEntries(): List<DexEntry> {
        entriesCallCount++
        failure?.let { throw it }
        return entries
    }

    override suspend fun pokemonDetail(variantSlug: String): PokemonDetail? {
        detailCallCount++
        failure?.let { throw it }
        return details[variantSlug]
    }
}
