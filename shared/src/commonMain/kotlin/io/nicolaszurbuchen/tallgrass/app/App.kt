package io.nicolaszurbuchen.tallgrass.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.nicolaszurbuchen.tallgrass.app.navigation.navConfig
import io.nicolaszurbuchen.tallgrass.design.theme.TallGrassTheme
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavGraph
import org.koin.compose.koinInject

@Composable
fun App() {
    val httpClient = koinInject<HttpClient>()
    remember(Unit) {
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components { add(KtorNetworkFetcherFactory(httpClient = { httpClient })) }
                .build()
        }
    }

    TallGrassTheme {
        NavGraph(config = navConfig)
    }
}
