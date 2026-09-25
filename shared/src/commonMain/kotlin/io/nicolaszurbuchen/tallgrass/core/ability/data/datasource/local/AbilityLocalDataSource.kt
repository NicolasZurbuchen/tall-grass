package io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility

interface AbilityLocalDataSource {
    suspend fun abilities(): List<Ability>

    suspend fun detail(slug: String): AbilityDetail?

    suspend fun holders(slug: String): List<AbilityHolder>

    suspend fun abilitiesFor(variantSlug: String): List<VariantAbility>
}
