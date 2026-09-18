package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository

class GetPokemonDetailUseCase(
    private val repository: PokedexRepository,
) {
    suspend operator fun invoke(variantSlug: String): PokemonDetail? = repository.pokemonDetail(variantSlug)
}
