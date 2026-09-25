package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AbilitiesDestination : NavKey

@Serializable
data object AbilitiesListDestination : AbilitiesDestination

/**
 * One ability, keyed by slug.
 *
 * Carries only the slug, and nothing travels into it. **This transition is a push**, which is what
 * #11 settled for both list-to-detail pairs in this round: a transition is a shared element when the
 * same image persists across the boundary, and an ability has no image on either side.
 *
 * A move's name does travel, and that is not a precedent for this one. The move card is tinted by its
 * type and so is the hero it opens, so the name crosses between two grounds of the same colour. An
 * ability card is a surface with dark text on it and the hero is not, so the same trick would be a
 * word changing colour in mid-air.
 */
@Serializable
data class AbilityDetailDestination(
    val slug: String,
) : AbilitiesDestination
