package io.nicolaszurbuchen.tallgrass.core.location.domain.repository

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry

interface LocationRepository {
    /** The eleven regions, in release order with the spin-off last. */
    suspend fun regions(): List<Region>

    /** One region and the games set in it, or null when no row carries that slug. */
    suspend fun regionDetail(slug: String): RegionDetail?

    /** One region's places, by name, which is the order its search bar implies. */
    suspend fun locationsIn(regionSlug: String): List<LocationSummary>

    /**
     * One region's Pokedex, as dex cards.
     *
     * Empty for Orre, which upstream has no regional dex for. Every card names the **region-native**
     * form, so Kanto's 37 is the Kantonian Vulpix and Alola's is the Alolan one -- see #5.
     */
    suspend fun regionDex(regionSlug: String): List<DexEntry>

    /** One place and the grid of its region's games, or null when no row carries that slug. */
    suspend fun locationDetail(slug: String): LocationDetail?

    /** Everything that can be met at one place in one game. */
    suspend fun encountersAt(
        locationSlug: String,
        versionSlug: String,
    ): List<Encounter>

    /** Everywhere one Pokemon can be met in one game. */
    suspend fun encountersFor(
        variantSlug: String,
        versionSlug: String,
    ): List<VariantEncounter>

    /** Which games have one Pokemon at all, and how else it can be got. */
    suspend fun availabilityFor(variantSlug: String): VariantAvailability

    /** Every condition value, so a selector can group its axes without reading them per route. */
    suspend fun conditions(): List<EncounterCondition>
}
