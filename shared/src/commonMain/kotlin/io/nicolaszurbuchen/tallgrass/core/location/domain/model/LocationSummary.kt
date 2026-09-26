package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One row in a region's Locations list.
 *
 * [versionCount] is how many games have anything to meet here, and **zero is an answer rather than a
 * gap**: Berry Forest exists in every Kanto game and carries encounters in two of them, and that
 * absence is itself what a reader came to find out. See #24.
 */
data class LocationSummary(
    val slug: String,
    val name: String,
    val category: LocationCategory,
    val versionCount: Int,
)
