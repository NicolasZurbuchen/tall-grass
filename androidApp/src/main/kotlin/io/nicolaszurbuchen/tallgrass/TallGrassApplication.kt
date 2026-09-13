package io.nicolaszurbuchen.tallgrass

import android.app.Application
import io.nicolaszurbuchen.tallgrass.app.di.initKoin
import io.nicolaszurbuchen.tallgrass.infra.di.platformModule
import org.koin.android.ext.koin.androidContext

class TallGrassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            additionalModules = listOf(platformModule),
            appDeclaration = {
                androidContext(this@TallGrassApplication)
            },
        )
    }
}
