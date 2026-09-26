package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One region, for the About tab.
 *
 * [blurb] is original prose written for this project. A region's own in-game description would be
 * flavour text, which #10 and #11 forbid shipping; eleven of these is the whole cost of not needing
 * it, and unlike the 1,025 a species-level version would need, it finishes.
 *
 * [versions] is every game set here, in grid order, and is derived rather than curated -- which is
 * what gives Kanto twelve and no 3DS row at all. [pokedexSize] is 0 for Orre, which upstream has no
 * regional dex for; the tab says so rather than substituting the national one.
 */
data class RegionDetail(
    val slug: String,
    val name: String,
    val nativeName: String?,
    val generation: Int,
    val blurb: String,
    val locationCount: Int,
    val pokedexSize: Int,
    val versions: List<GameVersion>,
)
