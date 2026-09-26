package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One place, for its own screen.
 *
 * [versions] is every game of the region, whether or not anything can be met here, because the grid
 * draws a cell for each and a grey one has to stay tappable -- that is how a reader confirms a
 * negative. [encounteredVersions] is the subset that comes up green.
 *
 * Scoping the grid to the region is what keeps it readable: Kanto shows twelve cells rather than
 * fifty, and Sword and Scarlet never appear on a Kanto route at all. See #24.
 */
data class LocationDetail(
    val slug: String,
    val name: String,
    val category: LocationCategory,
    val regionSlug: String,
    val regionName: String,
    val versions: List<GameVersion>,
    val encounteredVersions: Set<String>,
)
