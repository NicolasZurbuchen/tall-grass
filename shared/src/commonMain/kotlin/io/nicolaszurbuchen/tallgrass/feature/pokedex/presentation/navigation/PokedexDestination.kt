package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface PokedexDestination : NavKey

@Serializable
data object DexDestination : PokedexDestination

/**
 * One Pokemon, keyed by the slug of the card that was tapped.
 *
 * See [HeroHandoff] for [hero] and [DexQuery] for [query] — the first is what the card was drawing,
 * the second is which list it was drawn in.
 */
@Serializable
data class DetailDestination(
    val slug: String,
    val hero: HeroHandoff,
    val query: DexQuery,
) : PokedexDestination
