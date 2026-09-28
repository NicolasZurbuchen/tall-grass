package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetRegionLocationsUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(regionSlug: String): List<LocationSummary> = repository.locationsIn(regionSlug)
}
