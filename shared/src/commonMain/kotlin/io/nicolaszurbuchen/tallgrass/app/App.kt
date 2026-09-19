package io.nicolaszurbuchen.tallgrass.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.SingletonImageLoader
import io.ktor.client.HttpClient
import io.nicolaszurbuchen.tallgrass.app.navigation.navConfig
import io.nicolaszurbuchen.tallgrass.app.navigation.rememberAppNavTransitions
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.TallGrassTheme
import io.nicolaszurbuchen.tallgrass.infra.image.createImageLoader
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavGraph
import org.koin.compose.koinInject

@Composable
fun App() {
    val httpClient = koinInject<HttpClient>()
    remember(Unit) {
        // The loader is built in infra/image, which is also where the prefetch fills its disk cache.
        // Two builders would be two caches, and only one of them would be the one the screens read.
        SingletonImageLoader.setSafe { context ->
            createImageLoader(context = context, httpClient = httpClient, crossfadeMillis = AppDuration.INSTANT)
        }
    }

    TallGrassTheme {
        NavGraph(config = navConfig, transitions = rememberAppNavTransitions())
    }
}
