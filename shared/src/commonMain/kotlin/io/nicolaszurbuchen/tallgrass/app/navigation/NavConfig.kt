package io.nicolaszurbuchen.tallgrass.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation.AbilitiesListDestination
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.navigation.AbilityDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation.HomeRootDestination
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation.RegionDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.navigation.RegionsDestination
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.MoveDetailDestination
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.MovesListDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * How the back stack survives process death.
 *
 * **Every destination has to be named here.** `NavKey` is an interface rather than a sealed
 * hierarchy, so kotlinx.serialization cannot find the subclasses on its own — a destination that is
 * missing serializes fine right up until the moment Android saves state, and then throws
 * `Serializer for subclass '...' is not found in the polymorphic scope of 'NavKey'`.
 *
 * That is a crash on backgrounding the app, on the one screen the missing key is on, and nothing
 * earlier sees it: the app compiles, navigates and passes every test. It shipped that way for the
 * two Moves destinations. `NavConfigTest` now derives the list from the code and fails when one is
 * missing.
 *
 * Only concrete destinations are registered. The per-feature sealed interfaces above them are never
 * instances of anything.
 */
val navConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(HomeRootDestination::class)
                    subclass(DexDestination::class)
                    subclass(DetailDestination::class)
                    subclass(MovesListDestination::class)
                    subclass(MoveDetailDestination::class)
                    subclass(AbilitiesListDestination::class)
                    subclass(AbilityDetailDestination::class)
                    subclass(RegionsDestination::class)
                    subclass(RegionDetailDestination::class)
                }
            }
    }
