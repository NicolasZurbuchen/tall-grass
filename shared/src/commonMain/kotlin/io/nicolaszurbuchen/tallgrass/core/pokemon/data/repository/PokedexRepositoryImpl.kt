package io.nicolaszurbuchen.tallgrass.core.pokemon.data.repository

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.PokemonDetailLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository

class PokedexRepositoryImpl(
    private val dexLocalDataSource: DexLocalDataSource,
    private val detailLocalDataSource: PokemonDetailLocalDataSource,
) : PokedexRepository {
    override suspend fun dexEntries(): List<DexEntry> = dexLocalDataSource.dexEntries()

    override suspend fun pokemonDetail(variantSlug: String): PokemonDetail? = detailLocalDataSource.detail(variantSlug)
}
