package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
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

/** The navigation host. See CLAUDE.md § Animation is a constraint, not a finish for the wrapper. */
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
            // No inset padding here. The host used to hold every screen clear of the system bars,
            // which made a screen that wants colour behind the status bar impossible to write --
            // the padding is applied above the entry and cannot be undone below it. Insets are a
            // screen's own business now, and every screen has to say what it does about them.
            NavDisplay(
                backStack = backStack,
                modifier = modifier.background(color = MaterialTheme.colorScheme.background),
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
