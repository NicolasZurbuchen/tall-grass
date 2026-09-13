package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

/**
 * The navigation host, wrapped in a [SharedTransitionLayout].
 *
 * The wrapper is here from the first screen on purpose. A shared element needs matching keys on both
 * sides and source and target composables structured to share bounds, so adding the layout later
 * means reopening every screen that was written without it.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph(
    config: SavedStateConfiguration,
    modifier: Modifier = Modifier,
) {
    val navigator = koinInject<AppNavigator>()
    val initialRoute = koinInject<NavKey>(named("initialRoute"))
    val handlers = getKoin().getAll<NavKeyHandler>()

    val backStack =
        rememberNavBackStack(
            config,
            initialRoute,
        )

    LaunchedEffect(backStack) {
        navigator.attach(backStack)
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                modifier =
                    modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .systemBarsPadding(),
                onBack = { backStack.removeLastOrNull() },
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                entryProvider =
                    entryProvider {
                        handlers.forEach { handler ->
                            with(handler) { registerEntries() }
                        }
                    },
            )
        }
    }
}
