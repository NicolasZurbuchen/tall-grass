package io.nicolaszurbuchen.tallgrass.infra.image

import org.koin.dsl.module

val imageModule =
    module {
        single<ImagePrefetch> { CoilImagePrefetch(get()) }
    }
