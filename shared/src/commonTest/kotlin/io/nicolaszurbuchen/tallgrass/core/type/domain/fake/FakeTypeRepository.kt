package io.nicolaszurbuchen.tallgrass.core.type.domain.fake

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy
import io.nicolaszurbuchen.tallgrass.core.type.domain.repository.TypeRepository

class FakeTypeRepository(
    private val chart: Map<PokemonType, List<TypeEfficacy>> = emptyMap(),
) : TypeRepository {
    override suspend fun efficaciesAgainst(type: PokemonType): List<TypeEfficacy> = chart[type].orEmpty()
}
