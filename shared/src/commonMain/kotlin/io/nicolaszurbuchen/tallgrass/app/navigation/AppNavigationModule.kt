package io.nicolaszurbuchen.tallgrass.app.navigation

import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation.HomeNavKeyHandler
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.navigation.HomeRootDestination
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavKeyHandler
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

// Every feature's NavKeyHandler is bound here rather than in the feature's own di module. A feature
// module may only import from its own subtree, and the binding needs infra's NavKeyHandler on one
// side and the feature's handler on the other, so app/ -- the aggregator allowed to cross subtrees
// -- is the only place it can go.
val appNavigationModule =
    module {
        single<NavKey>(named("initialRoute")) { HomeRootDestination }

        singleOf(::HomeNavKeyHandler) { named("home") } bind NavKeyHandler::class
    }
