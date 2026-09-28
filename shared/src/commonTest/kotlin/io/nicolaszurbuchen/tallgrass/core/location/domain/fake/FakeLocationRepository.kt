package io.nicolaszurbuchen.tallgrass.core.location.domain.fake

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

/**
 * Counted per query rather than in total, on the same grounds as `FakeAbilityRepository`: a region's
 * detail reads four times over its three tabs, so one counter could not tell a retry from the second
 * tab of one load.
 *
 * The encounter reads are keyed by the pair they are asked with, because "what is on this route" and
 * "what is on it in *this game*" are different questions and a test that conflated them would pass
 * against a data source that ignored the version.
 */
class FakeLocationRepository(
    private var regions: List<Region> = emptyList(),
    private var regionDetails: Map<String, RegionDetail> = emptyMap(),
    private var locations: Map<String, List<LocationSummary>> = emptyMap(),
    private var dexes: Map<String, List<DexEntry>> = emptyMap(),
    private var locationDetails: Map<String, LocationDetail> = emptyMap(),
    private var encountersAt: Map<Pair<String, String>, List<Encounter>> = emptyMap(),
    private var encountersFor: Map<Pair<String, String>, List<VariantEncounter>> = emptyMap(),
    private var availability: Map<String, VariantAvailability> = emptyMap(),
    private var conditions: List<EncounterCondition> = emptyList(),
    private var failure: Throwable? = null,
) : LocationRepository {
    var regionsCallCount: Int = 0
        private set

    var regionDetailCallCount: Int = 0
        private set

    var locationsCallCount: Int = 0
        private set

    var regionDexCallCount: Int = 0
        private set

    var locationDetailCallCount: Int = 0
        private set

    var encountersAtCallCount: Int = 0
        private set

    var encountersForCallCount: Int = 0
        private set

    var availabilityCallCount: Int = 0
        private set

    var conditionsCallCount: Int = 0
        private set

    override suspend fun regions(): List<Region> {
        regionsCallCount++
        failure?.let { throw it }
        return regions
    }

    override suspend fun regionDetail(slug: String): RegionDetail? {
        regionDetailCallCount++
        failure?.let { throw it }
        return regionDetails[slug]
    }

    override suspend fun locationsIn(regionSlug: String): List<LocationSummary> {
        locationsCallCount++
        failure?.let { throw it }
        return locations[regionSlug].orEmpty()
    }

    override suspend fun regionDex(regionSlug: String): List<DexEntry> {
        regionDexCallCount++
        failure?.let { throw it }
        return dexes[regionSlug].orEmpty()
    }

    override suspend fun locationDetail(slug: String): LocationDetail? {
        locationDetailCallCount++
        failure?.let { throw it }
        return locationDetails[slug]
    }

    override suspend fun encountersAt(
        locationSlug: String,
        versionSlug: String,
    ): List<Encounter> {
        encountersAtCallCount++
        failure?.let { throw it }
        return encountersAt[locationSlug to versionSlug].orEmpty()
    }

    override suspend fun encountersFor(
        variantSlug: String,
        versionSlug: String,
    ): List<VariantEncounter> {
        encountersForCallCount++
        failure?.let { throw it }
        return encountersFor[variantSlug to versionSlug].orEmpty()
    }

    override suspend fun availabilityFor(variantSlug: String): VariantAvailability {
        availabilityCallCount++
        failure?.let { throw it }
        return availability[variantSlug]
            ?: VariantAvailability(
                variantSlug = variantSlug,
                captureMethods = emptyList(),
                versions = emptyList(),
                encounteredVersions = emptySet(),
            )
    }

    override suspend fun conditions(): List<EncounterCondition> {
        conditionsCallCount++
        failure?.let { throw it }
        return conditions
    }
}
