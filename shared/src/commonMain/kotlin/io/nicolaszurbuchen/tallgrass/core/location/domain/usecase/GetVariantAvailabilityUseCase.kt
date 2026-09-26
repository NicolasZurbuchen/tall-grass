package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetVariantAvailabilityUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(variantSlug: String): VariantAvailability = repository.availabilityFor(variantSlug)
}
