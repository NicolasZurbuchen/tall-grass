package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toStatsByVariantDomain
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PokemonDetailLocalDataSourceImpl(
    private val speciesQueries: Lazy<SpeciesQueries>,
    private val variantQueries: Lazy<VariantQueries>,
    private val dispatcher: CoroutineDispatcher,
) : PokemonDetailLocalDataSource {
    override suspend fun detail(variantSlug: String): PokemonDetail? =
        withContext(dispatcher) {
            val species =
                speciesQueries.value.selectSpeciesByVariantSlug(variantSlug).executeAsOneOrNull()
                    ?: return@withContext null

            val eggGroups = speciesQueries.value.selectEggGroupsBySpecies(species.dexNumber).executeAsList()
            val statsByVariant = variantQueries.value.selectStatsBySpecies(species.dexNumber).executeAsList().toStatsByVariantDomain()

            val variants =
                variantQueries.value
                    .selectVariantDetails(species.dexNumber)
                    .executeAsList()
                    .mapNotNull { row -> statsByVariant[row.slug]?.let(row::toDomain) }

            if (variants.isEmpty()) return@withContext null

            species.toDomain(eggGroups)?.let { PokemonDetail(species = it, variants = variants) }
        }
}
