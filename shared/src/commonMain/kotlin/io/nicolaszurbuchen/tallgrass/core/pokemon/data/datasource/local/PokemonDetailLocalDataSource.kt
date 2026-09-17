package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail

interface PokemonDetailLocalDataSource {
    /** One Pokemon and every form it has, or null when no variant carries that slug. */
    suspend fun detail(variantSlug: String): PokemonDetail?
}
