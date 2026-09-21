package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation

import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

/**
 * The identity a move card's name carries into the detail hero.
 *
 * **A move has no artwork, and #11's rule was written about artwork**: a transition is a shared
 * element when the same *image* persists across the boundary, and what it rejected was a cross-fade
 * dressed up as one. Text that genuinely travels is neither — the name and the pill on the card are
 * the same name and the same pill in the hero, drawn larger. What the rule forbids is pretending,
 * not moving something real.
 *
 * A function rather than a constant either side builds for itself: the card and the destination have
 * to produce exactly the same key or the transition silently does not run, and "silently" is the
 * part that makes a second construction site expensive.
 */
fun moveNameKey(slug: String): SharedElementKey = SharedElementKey(source = MOVE_SOURCE, id = slug)

/** The type pill travelling beside the name it qualifies. */
fun SharedElementKey.typeKey(): SharedElementKey = copy(source = source + TYPE_SUFFIX)

private const val MOVE_SOURCE = "move:name"

// Derived from the name's key rather than built beside it: the two elements leave one card and
// arrive at one hero, so there is one place where a wrong key is possible instead of two.
private const val TYPE_SUFFIX = ":type"
