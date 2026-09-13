package io.nicolaszurbuchen.tallgrass

import androidx.compose.ui.window.ComposeUIViewController
import io.nicolaszurbuchen.tallgrass.app.App
import io.nicolaszurbuchen.tallgrass.app.di.initKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    if (KoinPlatform.getKoinOrNull() == null) {
        initKoin()
    }
    return ComposeUIViewController { App() }
}
