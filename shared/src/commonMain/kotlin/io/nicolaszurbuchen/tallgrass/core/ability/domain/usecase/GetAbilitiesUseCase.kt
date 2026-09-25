package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.repository.AbilityRepository

class GetAbilitiesUseCase(
    private val repository: AbilityRepository,
) {
    suspend operator fun invoke(): List<Ability> = repository.abilities()
}
