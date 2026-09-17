package io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.SelectEfficacyAgainst
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy

/** Null when the attacking type is not one of the eighteen, which means the dataset is from another build. */
fun SelectEfficacyAgainst.toDomain(): TypeEfficacy? =
    PokemonType.fromSlug(damageTypeSlug)?.let { damageType ->
        TypeEfficacy(damageType = damageType, factorPercent = factorPercent.toInt())
    }
