package io.nicolaszurbuchen.tallgrass.core.location.data.repository

import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.LocationLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * The repository is a pass-through, so what is worth asserting is that it passes *through* -- that it
 * neither caches a read nor reorders one, and that both arguments of the two-argument reads arrive.
 */
class LocationRepositoryImplTest {
    @Test
    fun reads_reachTheDataSourceEveryTime() =
        runTest {
            val source = RecordingDataSource()
            val repository = LocationRepositoryImpl(source)

            repository.regions()
            repository.regions()

            assertEquals(2, source.regionsCallCount)
        }

    @Test
    fun encountersAt_passesBothThePlaceAndTheGame() =
        runTest {
            // Dropping the version here would answer "what is on this route" with every game at
            // once, which is the one question the app cannot be game-agnostic about.
            val source = RecordingDataSource()
            val repository = LocationRepositoryImpl(source)

            repository.encountersAt("kanto-route-1", "heartgold")

            assertEquals("kanto-route-1" to "heartgold", source.lastEncountersAt)
        }

    @Test
    fun encountersFor_passesBothThePokemonAndTheGame() =
        runTest {
            val source = RecordingDataSource()
            val repository = LocationRepositoryImpl(source)

            repository.encountersFor("pidgey", "red")

            assertEquals("pidgey" to "red", source.lastEncountersFor)
        }

    @Test
    fun regionDetail_returnsNullRatherThanInventingARegion() =
        runTest {
            val repository = LocationRepositoryImpl(RecordingDataSource())

            assertNull(repository.regionDetail("kanto"))
        }

    @Test
    fun reads_propagateFailure() =
        runTest {
            val repository = LocationRepositoryImpl(RecordingDataSource(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { repository.regions() }
        }

    private class RecordingDataSource(
        private val failure: Throwable? = null,
    ) : LocationLocalDataSource {
        var regionsCallCount: Int = 0
            private set

        var lastEncountersAt: Pair<String, String>? = null
            private set

        var lastEncountersFor: Pair<String, String>? = null
            private set

        override suspend fun regions(): List<Region> {
            regionsCallCount++
            failure?.let { throw it }
            return listOf(LocationFixtures.kanto)
        }

        override suspend fun regionDetail(slug: String): RegionDetail? = null

        override suspend fun locationsIn(regionSlug: String): List<LocationSummary> = emptyList()

        override suspend fun regionDex(regionSlug: String): List<DexEntry> = emptyList()

        override suspend fun locationDetail(slug: String): LocationDetail? = null

        override suspend fun encountersAt(
            locationSlug: String,
            versionSlug: String,
        ): List<Encounter> {
            lastEncountersAt = locationSlug to versionSlug
            return emptyList()
        }

        override suspend fun encountersFor(
            variantSlug: String,
            versionSlug: String,
        ): List<VariantEncounter> {
            lastEncountersFor = variantSlug to versionSlug
            return emptyList()
        }

        override suspend fun availabilityFor(variantSlug: String): VariantAvailability =
            VariantAvailability(variantSlug, emptyList(), emptyList(), emptySet())

        override suspend fun conditions(): List<EncounterCondition> = emptyList()
    }
}
