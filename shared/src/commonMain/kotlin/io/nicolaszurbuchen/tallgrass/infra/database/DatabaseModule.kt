package io.nicolaszurbuchen.tallgrass.infra.database

import org.koin.dsl.module

val databaseModule =
    module {
        single { createDatabase(get()) }
        single { createPokedexDatabase(get()) }
    }
