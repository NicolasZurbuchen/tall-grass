package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetLocationEncountersUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(
        locationSlug: String,
        versionSlug: String,
    ): List<Encounter> = repository.encountersAt(locationSlug, versionSlug)
}
