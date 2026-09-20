package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

/**
 * The identity a dex card's artwork carries into the detail hero.
 *
 * A function rather than a constant either side builds for itself: the card and the destination have
 * to produce exactly the same key or the transition silently does not run, and "silently" is the
 * part that makes a second construction site expensive. Search, locations and the team builder each
 * get their own here when they arrive — see #33.
 */
fun dexArtworkKey(slug: String): SharedElementKey = SharedElementKey(source = DEX_SOURCE, id = slug)

/** The name travelling beside the artwork it labels. */
fun SharedElementKey.nameKey(): SharedElementKey = copy(source = source + NAME_SUFFIX)

/** One type pill, identified by its slot so the two never match each other. */
fun SharedElementKey.typeKey(slot: Int): SharedElementKey = copy(source = source + TYPE_SUFFIX + slot)

private const val DEX_SOURCE = "dex"

// Derived from the artwork's key rather than built beside it: the four elements leave one card and
// arrive at one hero, so there is one place where a wrong key is possible instead of four.
private const val NAME_SUFFIX = ":name"
private const val TYPE_SUFFIX = ":type"
