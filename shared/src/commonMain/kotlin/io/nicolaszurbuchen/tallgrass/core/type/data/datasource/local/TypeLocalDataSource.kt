package io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy

interface TypeLocalDataSource {
    /** Every non-neutral cell of the chart with [type] on the receiving end. */
    suspend fun efficaciesAgainst(type: PokemonType): List<TypeEfficacy>
}
