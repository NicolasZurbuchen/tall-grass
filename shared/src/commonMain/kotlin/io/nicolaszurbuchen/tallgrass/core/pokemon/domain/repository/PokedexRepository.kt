package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail

interface PokedexRepository {
    /** Every variant that earns a grid card, in National Dex order with a species' forms adjacent. */
    suspend fun dexEntries(): List<DexEntry>

    /** One Pokemon and every form it has, or null when no variant carries that slug. */
    suspend fun pokemonDetail(variantSlug: String): PokemonDetail?
}
