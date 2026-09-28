package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetRegionDexUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(regionSlug: String): List<RegionDexEntry> = repository.regionDex(regionSlug)
}
