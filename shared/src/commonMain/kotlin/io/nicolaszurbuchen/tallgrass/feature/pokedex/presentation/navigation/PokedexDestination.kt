package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface PokedexDestination : NavKey

@Serializable
data object DexDestination : PokedexDestination

/** One Pokemon, keyed by the slug of the form that was tapped. See [HeroHandoff] for [hero]. */
@Serializable
data class DetailDestination(
    val slug: String,
    val hero: HeroHandoff,
) : PokedexDestination
