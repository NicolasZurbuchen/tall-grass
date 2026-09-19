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

/**
 * The identity the card's colour carries into the ground behind the hero.
 *
 * A second key rather than a second use of [dexArtworkKey], because they are two elements travelling
 * together and a shared element may only be matched once. Same id, so both halves of a card move as
 * one Pokemon.
 */
fun dexTintKey(slug: String): SharedElementKey = SharedElementKey(source = DEX_TINT_SOURCE, id = slug)

private const val DEX_SOURCE = "dex"

private const val DEX_TINT_SOURCE = "dex-tint"

/**
 * The artwork's height in the shared-element overlay.
 *
 * The colour travels at the same time and ends up filling the screen, so without this the Pokemon
 * spends the whole flight behind it. Both halves read it from here for the same reason they read
 * their keys from here.
 */
const val ARTWORK_OVERLAY_Z = 1f
