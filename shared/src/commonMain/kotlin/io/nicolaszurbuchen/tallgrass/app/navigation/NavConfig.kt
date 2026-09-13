package io.nicolaszurbuchen.tallgrass.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation.HomeRootDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val navConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(HomeRootDestination::class)
                }
            }
    }
