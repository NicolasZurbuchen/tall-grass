package io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class TypeLocalDataSourceImpl(
    private val queries: TypeQueries,
    private val dispatcher: CoroutineDispatcher,
) : TypeLocalDataSource {
    override suspend fun efficaciesAgainst(type: PokemonType): List<TypeEfficacy> =
        withContext(dispatcher) {
            queries
                .selectEfficacyAgainst(type.slug)
                .executeAsList()
                .mapNotNull { it.toDomain() }
        }
}
