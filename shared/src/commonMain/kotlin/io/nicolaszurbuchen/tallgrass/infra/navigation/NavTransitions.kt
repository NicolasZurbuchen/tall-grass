package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

/**
 * How the host moves between screens.
 *
 * Taken as a parameter rather than written here, because the numbers and curves belong to the design
 * system and `infra/` may not import it. This is the mechanism; the motion is supplied by whoever
 * composes the app.
 */
class NavTransitions(
    val forward: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform,
    val back: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform,
    /** The `Int` is the swipe edge the gesture began from. */
    val predictiveBack: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform,
) {
    companion object {
        /**
         * A plain cross-dissolve, for a host with nothing better to say. Also what the app falls
         * back to under reduced motion, where a screen sliding in is the thing being asked about.
         */
        val Fade: NavTransitions =
            NavTransitions(
                forward = { fadeIn() togetherWith fadeOut() },
                back = { fadeIn() togetherWith fadeOut() },
                predictiveBack = { fadeIn() togetherWith fadeOut() },
            )
    }
}
