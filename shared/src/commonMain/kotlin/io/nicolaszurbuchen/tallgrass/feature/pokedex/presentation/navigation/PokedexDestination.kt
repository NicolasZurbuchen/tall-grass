package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface PokedexDestination : NavKey

@Serializable
data object DexDestination : PokedexDestination

/**
 * One Pokemon, keyed by the slug of the dex card that leads to it.
 *
 * See [HeroHandoff] for [hero] and [DexQuery] for [query] — the first is what the card was drawing,
 * the second is which list it was drawn in.
 *
 * **[slug] is a card and [formSlug] is a form, and the two are only the same thing most of the time.**
 * Tapping a card in the grid opens its own default form and leaves [formSlug] null. Arriving from a
 * move's Learned by tab does not: Alolan Exeggutor is reached through Exeggutor's card, so the card
 * is what the carousel swipes along and the form is what the screen opens on. Collapsing them put the
 * variant's slug where a card belonged, and a variant is in no dex list -- the carousel found nothing
 * either side of it and the swipe stopped working.
 */
@Serializable
data class DetailDestination(
    val slug: String,
    val hero: HeroHandoff,
    val query: DexQuery,
    val formSlug: String? = null,
) : PokedexDestination
