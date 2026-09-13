package io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeDestination : NavKey

@Serializable
data object HomeRootDestination : HomeDestination
