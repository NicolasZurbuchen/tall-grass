package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper.toEvYieldByVariantDomain
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

            // One read, two records: the six rows carry a variant's base stats and what defeating it
            // awards, and both are wanted for every form before the switcher can move without
            // hitting the database again.
            val statRows = variantQueries.value.selectStatsBySpecies(species.dexNumber).executeAsList()
            val statsByVariant = statRows.toStatsByVariantDomain()
            val evYieldByVariant = statRows.toEvYieldByVariantDomain()

            val variants =
                variantQueries.value
                    .selectVariantDetails(species.dexNumber)
                    .executeAsList()
                    .mapNotNull { row ->
                        statsByVariant[row.slug]?.let { stats ->
                            row.toDomain(stats = stats, evYield = evYieldByVariant[row.slug])
                        }
                    }

            if (variants.isEmpty()) return@withContext null

            species.toDomain(eggGroups)?.let { PokemonDetail(species = it, variants = variants) }
        }
}
