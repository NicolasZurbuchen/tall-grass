package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

class GetRegionDexUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(regionSlug: String): List<DexEntry> = repository.regionDex(regionSlug)
}
