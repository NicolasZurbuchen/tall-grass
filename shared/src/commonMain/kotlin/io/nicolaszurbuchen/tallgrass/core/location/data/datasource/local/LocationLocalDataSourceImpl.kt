package io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.mapper.toCaptureMethodsDomain
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationCategory
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class LocationLocalDataSourceImpl(
    private val regionQueries: Lazy<RegionQueries>,
    private val locationQueries: Lazy<LocationQueries>,
    private val encounterQueries: Lazy<EncounterQueries>,
    private val dispatcher: CoroutineDispatcher,
) : LocationLocalDataSource {
    // Two reads rather than one join: a card needs its pair of artwork rows, and joining would
    // return each region twice and leave the caller to fold it back.
    override suspend fun regions(): List<Region> =
        withContext(dispatcher) {
            val boxArt =
                regionQueries.value
                    .selectAllBoxArt()
                    .executeAsList()
                    .groupBy({ it.regionSlug }, { it.artworkUrl })

            regionQueries.value
                .selectRegions()
                .executeAsList()
                .map { it.toDomain(boxArt[it.slug].orEmpty()) }
        }

    override suspend fun regionDetail(slug: String): RegionDetail? =
        withContext(dispatcher) {
            val row = regionQueries.value.selectRegion(slug).executeAsOneOrNull() ?: return@withContext null
            val versions = regionQueries.value.selectRegionVersions(slug).executeAsList().mapNotNull { it.toDomain() }

            RegionDetail(
                slug = row.slug,
                name = row.name,
                nativeName = row.nativeName,
                generation = row.generation.toInt(),
                blurb = row.blurb,
                locationCount = row.locationCount.toInt(),
                pokedexSize = regionQueries.value.selectRegionDex(slug).executeAsList().size,
                versions = versions,
            )
        }

    override suspend fun locationsIn(regionSlug: String): List<LocationSummary> =
        withContext(dispatcher) {
            locationQueries.value
                .selectLocationsByRegion(regionSlug)
                .executeAsList()
                .map { it.toDomain() }
        }

    override suspend fun regionDex(regionSlug: String): List<RegionDexEntry> =
        withContext(dispatcher) {
            regionQueries.value
                .selectRegionDex(regionSlug)
                .executeAsList()
                .mapNotNull { it.toDomain() }
        }

    override suspend fun locationDetail(slug: String): LocationDetail? =
        withContext(dispatcher) {
            val row = locationQueries.value.selectLocation(slug).executeAsOneOrNull() ?: return@withContext null

            LocationDetail(
                slug = row.slug,
                name = row.name,
                category = LocationCategory.fromName(row.category),
                regionSlug = row.regionSlug,
                regionName = row.regionName,
                // Every game of the region, not only the ones with rows: a grey cell has to be
                // there to be tapped, which is how a reader confirms a negative. See #9.
                versions = regionQueries.value.selectRegionVersions(row.regionSlug).executeAsList().mapNotNull { it.toDomain() },
                encounteredVersions =
                    encounterQueries.value
                        .selectEncounteredVersionsAtLocation(slug)
                        .executeAsList()
                        .toSet(),
            )
        }

    override suspend fun encountersAt(
        locationSlug: String,
        versionSlug: String,
    ): List<Encounter> =
        withContext(dispatcher) {
            val conditions =
                encounterQueries.value
                    .selectConditionsAtLocation(locationSlug, versionSlug)
                    .executeAsList()
                    .groupBy({ it.encounterId }, { it.conditionSlug })

            encounterQueries.value
                .selectEncountersAtLocation(locationSlug, versionSlug)
                .executeAsList()
                .mapNotNull { it.toDomain(conditions[it.id].orEmpty()) }
        }

    override suspend fun encountersFor(
        variantSlug: String,
        versionSlug: String,
    ): List<VariantEncounter> =
        withContext(dispatcher) {
            val conditions =
                encounterQueries.value
                    .selectConditionsForVariant(variantSlug, versionSlug)
                    .executeAsList()
                    .groupBy({ it.encounterId }, { it.conditionSlug })

            encounterQueries.value
                .selectEncountersForVariant(variantSlug, versionSlug)
                .executeAsList()
                .map { it.toDomain(conditions[it.id].orEmpty()) }
        }

    override suspend fun availabilityFor(variantSlug: String): VariantAvailability =
        withContext(dispatcher) {
            val versions = regionQueries.value.selectGameVersions().executeAsList().mapNotNull { it.toDomain() }
            val encountered = encounterQueries.value.selectEncounteredVersionsForVariant(variantSlug).executeAsList().toSet()
            val methods = encounterQueries.value.selectMethodsForVariant(variantSlug).executeAsList().toSet()

            // Null only when the encounter table is empty, which cannot happen in a shipped dataset
            // and does happen in a test that built one. Zero reads as "no generation has data", and
            // the pills then say nothing rather than calling every Pokemon a transfer.
            val newest = encounterQueries.value.newestGenerationWithEncounters().executeAsOne().max?.toInt() ?: 0

            VariantAvailability(
                variantSlug = variantSlug,
                captureMethods =
                    methods.toCaptureMethodsDomain(
                        generationsPresent = versions.filter { it.slug in encountered }.map { it.generation }.toSet(),
                        newestGenerationWithData = newest,
                    ),
                versions = versions,
                encounteredVersions = encountered,
            )
        }

    override suspend fun conditions(): List<EncounterCondition> =
        withContext(dispatcher) {
            encounterQueries.value
                .selectConditionValues()
                .executeAsList()
                .map { it.toDomain() }
        }
}
