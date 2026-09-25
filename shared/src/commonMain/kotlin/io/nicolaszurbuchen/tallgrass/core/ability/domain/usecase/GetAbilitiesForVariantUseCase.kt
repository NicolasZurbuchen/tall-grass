package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility
import io.nicolaszurbuchen.tallgrass.core.ability.domain.repository.AbilityRepository

class GetAbilitiesForVariantUseCase(
    private val repository: AbilityRepository,
) {
    suspend operator fun invoke(variantSlug: String): List<VariantAbility> = repository.abilitiesFor(variantSlug)
}
