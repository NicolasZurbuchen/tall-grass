package io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter

interface LocationLocalDataSource {
    suspend fun regions(): List<Region>

    suspend fun regionDetail(slug: String): RegionDetail?

    suspend fun locationsIn(regionSlug: String): List<LocationSummary>

    suspend fun regionDex(regionSlug: String): List<RegionDexEntry>

    suspend fun locationDetail(slug: String): LocationDetail?

    suspend fun encountersAt(
        locationSlug: String,
        versionSlug: String,
    ): List<Encounter>

    suspend fun encountersFor(
        variantSlug: String,
        versionSlug: String,
    ): List<VariantEncounter>

    suspend fun availabilityFor(variantSlug: String): VariantAvailability

    suspend fun conditions(): List<EncounterCondition>
}
