package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toStatsByVariantDomain
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PokemonDetailLocalDataSourceImpl(
    private val speciesQueries: SpeciesQueries,
    private val variantQueries: VariantQueries,
    private val dispatcher: CoroutineDispatcher,
) : PokemonDetailLocalDataSource {
    override suspend fun detail(variantSlug: String): PokemonDetail? =
        withContext(dispatcher) {
            val species =
                speciesQueries.selectSpeciesByVariantSlug(variantSlug).executeAsOneOrNull()
                    ?: return@withContext null

            val eggGroups = speciesQueries.selectEggGroupsBySpecies(species.dexNumber).executeAsList()
            val statsByVariant = variantQueries.selectStatsBySpecies(species.dexNumber).executeAsList().toStatsByVariantDomain()

            val variants =
                variantQueries
                    .selectVariantDetails(species.dexNumber)
                    .executeAsList()
                    .mapNotNull { row -> statsByVariant[row.slug]?.let(row::toDomain) }

            if (variants.isEmpty()) return@withContext null

            species.toDomain(eggGroups)?.let { PokemonDetail(species = it, variants = variants) }
        }
}
