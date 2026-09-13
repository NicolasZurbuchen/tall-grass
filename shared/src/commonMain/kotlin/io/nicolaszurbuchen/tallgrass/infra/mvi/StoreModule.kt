package io.nicolaszurbuchen.tallgrass.infra.mvi

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.dsl.module

val storeModule =
    module {
        single<StoreFactory> { DefaultStoreFactory() }
    }
