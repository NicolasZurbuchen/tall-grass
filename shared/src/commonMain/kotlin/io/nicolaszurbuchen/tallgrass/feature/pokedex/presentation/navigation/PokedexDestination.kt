package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface PokedexDestination : NavKey

@Serializable
data object DexDestination : PokedexDestination
