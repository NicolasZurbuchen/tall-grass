package io.nicolaszurbuchen.tallgrass.core.type.data.repository

import io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.TypeLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy
import io.nicolaszurbuchen.tallgrass.core.type.domain.repository.TypeRepository

class TypeRepositoryImpl(
    private val localDataSource: TypeLocalDataSource,
) : TypeRepository {
    override suspend fun efficaciesAgainst(type: PokemonType): List<TypeEfficacy> = localDataSource.efficaciesAgainst(type)
}
