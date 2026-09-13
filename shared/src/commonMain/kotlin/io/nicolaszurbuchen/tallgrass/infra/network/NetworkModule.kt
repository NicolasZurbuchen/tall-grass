package io.nicolaszurbuchen.tallgrass.infra.network

import org.koin.dsl.module

val networkModule =
    module {
        single { createHttpClient() }
    }
