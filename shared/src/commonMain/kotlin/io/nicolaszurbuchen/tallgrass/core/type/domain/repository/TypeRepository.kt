package io.nicolaszurbuchen.tallgrass.core.type.domain.repository

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy

interface TypeRepository {
    /** Every non-neutral cell of the chart with [type] on the receiving end. */
    suspend fun efficaciesAgainst(type: PokemonType): List<TypeEfficacy>
}
