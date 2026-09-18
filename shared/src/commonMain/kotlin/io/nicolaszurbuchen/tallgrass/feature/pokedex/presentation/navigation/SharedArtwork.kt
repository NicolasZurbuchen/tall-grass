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

private const val DEX_SOURCE = "dex"
