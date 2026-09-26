package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface LocationsDestination : NavKey

@Serializable
data object RegionsDestination : LocationsDestination

/**
 * One region, keyed by slug.
 *
 * Carries only the slug. #11 classes the region card to region detail transition as a **shared
 * element** on the box-art pair, which is the rule it settled -- a transition is a shared element
 * when the same image persists across the boundary. That is a later pass: the artwork is the same two
 * URLs on both sides, so the keys can be matched without either screen changing shape, and #12 is
 * where the transition itself lands.
 */
@Serializable
data class RegionDetailDestination(
    val slug: String,
) : LocationsDestination

/**
 * One place, keyed by slug.
 *
 * **A push, not a shared element.** #11 classifies it that way for the region-to-location move, and
 * the rule agrees: nothing travels across the boundary, because a location row carries a coloured
 * marker rather than an image. The encounter rows *inside* this screen are the other way round --
 * each carries a sprite the Pokemon detail draws, which is the shared element #11 does list.
 */
@Serializable
data class LocationDetailDestination(
    val slug: String,
) : LocationsDestination
