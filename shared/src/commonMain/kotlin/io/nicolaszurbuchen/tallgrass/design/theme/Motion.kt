package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.MotionDurationScale

// DECISIONS.md § Three curves and five durations, measured rather than chosen
object AppEasing {
    // Flutter's `Curves.easeOutQuint` -- the pager settle, and the stat bars.
    val EaseOutQuint: Easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    // Flutter's `Curves.easeInOut`, which Compose already ships under another name.
    val EaseInOut: Easing = FastOutSlowInEasing

    // The design mock's signature curve. Used only where the Flutter app has no equivalent.
    val Emphasized: Easing = CubicBezierEasing(0.22f, 0.9f, 0.24f, 1f)
}

// Milliseconds, which is the unit `tween` takes.
// DECISIONS.md § Three curves and five durations, measured rather than chosen
object AppDuration {
    // Flutter's image fade. Short enough to read as "already there" rather than as a transition.
    const val INSTANT: Int = 120

    // Flutter's slide controller.
    const val SHORT: Int = 300

    // The one derived value, sitting where a step between SHORT and LONG was needed.
    const val MEDIUM: Int = 450

    // Flutter's `AnimatedPadding` on the pager -- the settle this app is trying to match.
    const val LONG: Int = 600

    // Flutter's pokeball rotation. One full turn.
    const val LOOP: Int = 5000
}

// DECISIONS.md § Three curves and five durations, measured rather than chosen
object AppStagger {
    // The mock's `i * 0.055s`.
    const val STEP_MILLIS: Int = 55

    // Beyond this many, every item shares the last delay.
    const val MAX_ITEMS: Int = 8

    /**
     * How long the item at [viewportIndex] waits before it enters.
     *
     * [viewportIndex] is the item's position in the **visible viewport**, never its index in the
     * list. Passing an absolute index is the mistake this cap exists to make survivable, and it is
     * still wrong: the item would enter at the right time for a list it is not in.
     */
    fun delayFor(viewportIndex: Int): Int = STEP_MILLIS * viewportIndex.coerceIn(0, MAX_ITEMS - 1)
}

/**
 * Whether the system is asking for motion to be kept to a minimum.
 *
 * True only when the platform reports a scale of exactly zero. Compose already scales animation
 * *durations* by that factor on its own; what a caller needs this for is the categorical choices
 * duration cannot express, such as stopping a loop rather than running it instantly.
 *
 * **Absent means not reduced.** The element is installed by the platform's window recomposer, so a
 * composition running outside one — a screenshot harness, a test with a bare effect context — gets
 * full motion rather than an exception.
 *
 * Must be called during composition: on Android the scale factor is snapshot state, so a screen that
 * reads it here recomposes when the setting changes underneath it.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    // The scope is only a handle on the composition's coroutine context, which is where the platform
    // puts the scale. Nothing is launched in it.
    val scope = rememberCoroutineScope()

    return scope.coroutineContext[MotionDurationScale]?.scaleFactor == 0f
}
