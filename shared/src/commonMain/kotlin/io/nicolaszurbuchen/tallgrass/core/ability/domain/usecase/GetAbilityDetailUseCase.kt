package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.repository.AbilityRepository

class GetAbilityDetailUseCase(
    private val repository: AbilityRepository,
) {
    suspend operator fun invoke(slug: String): AbilityDetail? = repository.abilityDetail(slug)
}
