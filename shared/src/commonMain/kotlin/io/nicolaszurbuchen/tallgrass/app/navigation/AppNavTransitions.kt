package io.nicolaszurbuchen.tallgrass.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.rememberReducedMotion
import io.nicolaszurbuchen.tallgrass.infra.navigation.NavTransitions
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementEntry

/**
 * The app's screen-to-screen motion — the mock's `scrIn` and `scrBack`.
 *
 * Under reduced motion every one of these becomes a cross-dissolve.
 *
 * A destination marked by [SharedElementEntry] cross-dissolves; everything else slides.
 *
 * DECISIONS.md § A screen reached from an element does not also arrive from the side
 * DECISIONS.md § The navigation host takes its motion as a parameter
 * DECISIONS.md § Reduced motion is answered per category, not left to the duration scale
 */
@Composable
fun rememberAppNavTransitions(): NavTransitions {
    val reducedMotion = rememberReducedMotion()

    return remember(reducedMotion) {
        if (reducedMotion) return@remember NavTransitions.Fade

        val spec = tween<Float>(durationMillis = AppDuration.SHORT, easing = AppEasing.EaseInOut)
        val slide = tween<IntOffset>(durationMillis = AppDuration.SHORT, easing = AppEasing.EaseInOut)
        val dissolve = fadeIn(spec) togetherWith fadeOut(spec)

        NavTransitions(
            forward = {
                if (SharedElementEntry.marks(targetState)) {
                    dissolve
                } else {
                    slideInHorizontally(slide) { width -> width } + fadeIn(spec) togetherWith
                        // The outgoing screen moves a fraction of the distance the incoming one
                        // covers, so it reads as being pushed rather than as two screens racing
                        // each other.
                        slideOutHorizontally(slide) { width -> -width / PARALLAX_DIVISOR } + fadeOut(spec)
                }
            },
            back = {
                // The screen being left is the one that was reached from an element, so it is the
                // initial state that decides here.
                if (SharedElementEntry.marks(initialState)) {
                    dissolve
                } else {
                    slideInHorizontally(slide) { width -> -width / PARALLAX_DIVISOR } + fadeIn(spec) togetherWith
                        slideOutHorizontally(slide) { width -> width } + fadeOut(spec)
                }
            },
            // The gesture drives this one, so it carries no duration of its own: the fraction of the
            // swipe is the fraction of the transition, and a tween on top would fight the finger.
            predictiveBack = {
                if (SharedElementEntry.marks(initialState)) {
                    dissolve
                } else {
                    fadeIn(spec) togetherWith slideOutHorizontally { width -> width } + fadeOut(spec)
                }
            },
        )
    }
}

// The outgoing screen travels a quarter of the width. Any more and it reads as leaving on its own
// errand rather than as being covered.
private const val PARALLAX_DIVISOR = 4
