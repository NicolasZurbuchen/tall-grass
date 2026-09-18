package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey
import kotlinx.serialization.Serializable

/**
 * What the tapped card was already drawing.
 *
 * Carried on the destination so the detail screen can draw the same thing on its first frame,
 * before it has read anything. A hero that resolves its own artwork renders empty for a frame, and
 * one that waits on the database to learn its colour changes colour in front of the reader — either
 * is the flicker the shared element exists to remove. See #11 and #33.
 *
 * [primaryTypeSlug] rather than a packed colour: the colour is a decision the design system makes
 * from the type, and a destination that carried an ARGB value would be remembering the answer to a
 * question it is not allowed to ask.
 */
@Serializable
data class HeroHandoff(
    val artworkUrl: String,
    val primaryTypeSlug: String,
    val sharedElementKey: SharedElementKey,
)
