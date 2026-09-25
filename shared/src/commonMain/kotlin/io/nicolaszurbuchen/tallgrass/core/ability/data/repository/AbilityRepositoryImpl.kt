package io.nicolaszurbuchen.tallgrass.core.ability.data.repository

import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.AbilityLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility
import io.nicolaszurbuchen.tallgrass.core.ability.domain.repository.AbilityRepository

class AbilityRepositoryImpl(
    private val localDataSource: AbilityLocalDataSource,
) : AbilityRepository {
    override suspend fun abilities(): List<Ability> = localDataSource.abilities()

    override suspend fun abilityDetail(slug: String): AbilityDetail? = localDataSource.detail(slug)

    override suspend fun abilityHolders(slug: String): List<AbilityHolder> = localDataSource.holders(slug)

    override suspend fun abilitiesFor(variantSlug: String): List<VariantAbility> = localDataSource.abilitiesFor(variantSlug)
}
