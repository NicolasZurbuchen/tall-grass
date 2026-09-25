package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.ability.domain.repository.AbilityRepository

class GetAbilityHoldersUseCase(
    private val repository: AbilityRepository,
) {
    suspend operator fun invoke(slug: String): List<AbilityHolder> = repository.abilityHolders(slug)
}
