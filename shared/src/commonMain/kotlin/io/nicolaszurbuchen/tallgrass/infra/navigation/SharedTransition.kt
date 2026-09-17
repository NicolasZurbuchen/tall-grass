package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The [SharedTransitionScope] opened by the navigation host.
 *
 * Fails rather than defaulting to null: reading it outside the host is a wiring mistake, and a null
 * default turns that into a transition that silently does not run.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope> {
        error("No SharedTransitionScope available. This must be read inside the navigation host.")
    }
