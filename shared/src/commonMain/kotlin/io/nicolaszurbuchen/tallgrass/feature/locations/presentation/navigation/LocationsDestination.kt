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
