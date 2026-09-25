package io.nicolaszurbuchen.tallgrass.core.ability.domain.repository

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder

interface AbilityRepository {
    /** Every main-series ability, by name. */
    suspend fun abilities(): List<Ability>

    /** One ability, or null when no row carries that slug. */
    suspend fun abilityDetail(slug: String): AbilityDetail?

    /** Which Pokemon have one ability, in National Dex order. */
    suspend fun abilityHolders(slug: String): List<AbilityHolder>
}
