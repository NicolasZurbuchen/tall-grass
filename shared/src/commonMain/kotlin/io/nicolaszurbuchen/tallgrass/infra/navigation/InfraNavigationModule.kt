package io.nicolaszurbuchen.tallgrass.infra.navigation

import org.koin.dsl.module

val infraNavigationModule =
    module {
        single { AppNavigator() }
    }
