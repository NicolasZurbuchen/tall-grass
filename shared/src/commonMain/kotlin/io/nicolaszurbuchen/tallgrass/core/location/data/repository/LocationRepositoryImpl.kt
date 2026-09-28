package io.nicolaszurbuchen.tallgrass.core.location.data.repository

import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.LocationLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.repository.LocationRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

class LocationRepositoryImpl(
    private val localDataSource: LocationLocalDataSource,
) : LocationRepository {
    override suspend fun regions(): List<Region> = localDataSource.regions()

    override suspend fun regionDetail(slug: String): RegionDetail? = localDataSource.regionDetail(slug)

    override suspend fun locationsIn(regionSlug: String): List<LocationSummary> = localDataSource.locationsIn(regionSlug)

    override suspend fun regionDex(regionSlug: String): List<DexEntry> = localDataSource.regionDex(regionSlug)

    override suspend fun locationDetail(slug: String): LocationDetail? = localDataSource.locationDetail(slug)

    override suspend fun encountersAt(
        locationSlug: String,
        versionSlug: String,
    ): List<Encounter> = localDataSource.encountersAt(locationSlug, versionSlug)

    override suspend fun encountersFor(
        variantSlug: String,
        versionSlug: String,
    ): List<VariantEncounter> = localDataSource.encountersFor(variantSlug, versionSlug)

    override suspend fun availabilityFor(variantSlug: String): VariantAvailability = localDataSource.availabilityFor(variantSlug)

    override suspend fun conditions(): List<EncounterCondition> = localDataSource.conditions()
}
