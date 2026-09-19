package io.nicolaszurbuchen.tallgrass.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import io.nicolaszurbuchen.tallgrass.app.navigation.navConfig
import io.nicolaszurbuchen.tallgrass.app.navigation.rememberAppNavTransitions
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
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
                // The mock fades an image in over 120ms. Coil does it for every AsyncImage in the
                // app from here, so no call site has to remember to ask.
                .crossfade(AppDuration.INSTANT)
                .build()
        }
    }

    TallGrassTheme {
        NavGraph(config = navConfig, transitions = rememberAppNavTransitions())
    }
}
