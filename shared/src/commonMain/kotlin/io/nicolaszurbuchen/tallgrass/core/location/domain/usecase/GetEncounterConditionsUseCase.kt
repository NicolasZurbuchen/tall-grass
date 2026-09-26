package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetEncounterConditionsUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(): List<EncounterCondition> = repository.conditions()
}
