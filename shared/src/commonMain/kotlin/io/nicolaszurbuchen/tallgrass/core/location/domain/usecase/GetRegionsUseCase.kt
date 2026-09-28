package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetRegionsUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(): List<Region> = repository.regions()
}
