package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository

class GetVariantEncountersUseCase(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(
        variantSlug: String,
        versionSlug: String,
    ): List<VariantEncounter> = repository.encountersFor(variantSlug, versionSlug)
}
