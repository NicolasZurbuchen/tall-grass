package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetRegionDetailUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(slug: String): RegionDetail? = repository.regionDetail(slug)
}
