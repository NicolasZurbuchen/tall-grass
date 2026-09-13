package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The [SharedTransitionScope] opened by the navigation host, so a screen can match an element
 * against its counterpart on the screen it came from.
 *
 * This exists as a composition local rather than a parameter because a shared element is declared
 * deep inside a screen — on the card's image, not on the screen composable — and threading the scope
 * down to it would put a parameter on every layer in between.
 *
 * It fails loudly rather than defaulting to null: reading it outside the host is a wiring mistake,
 * and a null default turns that into a transition that silently does not run.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope> {
        error("No SharedTransitionScope available. This must be read inside the navigation host.")
    }
