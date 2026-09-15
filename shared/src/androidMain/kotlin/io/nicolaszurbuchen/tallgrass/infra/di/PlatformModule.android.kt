package io.nicolaszurbuchen.tallgrass.infra.di

import io.nicolaszurbuchen.tallgrass.infra.database.DatabaseDriverFactory
import io.nicolaszurbuchen.tallgrass.infra.database.PokedexDriverFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule =
    module {
        singleOf(::DatabaseDriverFactory)
        singleOf(::PokedexDriverFactory)
    }
