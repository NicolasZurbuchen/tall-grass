package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetLocationDetailUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(slug: String): LocationDetail? = repository.locationDetail(slug)
}
