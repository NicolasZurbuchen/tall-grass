package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.MotionDurationScale

/**
 * The curve-based exceptions to Material's motion scheme.
 *
 * M3's springs are the default vocabulary and most of the app should keep using them. These three
 * exist because matching the Flutter reference is an explicit goal of this project and a spring
 * cannot reproduce a 600ms `easeOutQuint` settle. See #12.
 */
object AppEasing {
    /** Flutter's `Curves.easeOutQuint` — the pager settle, and the stat bars. */
    val EaseOutQuint: Easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    /** Flutter's `Curves.easeInOut`, which Compose already ships under another name. */
    val EaseInOut: Easing = FastOutSlowInEasing

    /** The design mock's signature curve. Used only where the Flutter app has no equivalent. */
    val Emphasized: Easing = CubicBezierEasing(0.22f, 0.9f, 0.24f, 1f)
}

/**
 * Durations in milliseconds, which is the unit `tween` takes.
 *
 * Four of the five are measured from the Flutter reference rather than chosen. [Medium] is the one
 * derived value, sitting where a step between [Short] and [Long] was needed.
 */
object AppDuration {
    /** Flutter's image fade. Short enough to read as "already there" rather than as a transition. */
    const val INSTANT: Int = 120

    /** Flutter's slide controller. */
    const val SHORT: Int = 300

    const val MEDIUM: Int = 450

    /** Flutter's `AnimatedPadding` on the pager — the settle this app is trying to match. */
    const val LONG: Int = 600

    /** Flutter's pokeball rotation. One full turn. */
    const val LOOP: Int = 5000
}

/**
 * The entrance stagger.
 *
 * **Index within the visible viewport, never the absolute list index.** The dex is 1,082 cards; at
 * 55ms each, card 500 would enter twenty-seven seconds after card 0, which is not a stagger but a
 * bug that looks like a hang.
 */
object AppStagger {
    /** The mock's `i * 0.055s`. */
    const val STEP_MILLIS: Int = 55

    /** Beyond this many, every item shares the last delay. */
    const val MAX_ITEMS: Int = 8

    /**
     * How long the item at [viewportIndex] waits before it enters.
     *
     * Capped, so the ninth visible item and every one after it start together at 385ms and a full
     * entrance takes that plus one item's own duration — the same length whether the viewport holds
     * nine cards or ninety.
     */
    fun delayFor(viewportIndex: Int): Int = STEP_MILLIS * viewportIndex.coerceIn(0, MAX_ITEMS - 1)
}

/**
 * Whether the system is asking for motion to be kept to a minimum.
 *
 * Compose already scales animation *durations* by this factor on its own, which is why nothing had
 * to read it until now. What it cannot do is make the categorical choices #12 asks for — a decorative
 * loop should stop rather than run instantly, and a shared element should cross-fade rather than
 * snap — and those need the answer as a boolean.
 *
 * Read during composition so the screen recomposes if the setting changes underneath it: on Android
 * the scale factor is snapshot state backed by `Settings.Global.ANIMATOR_DURATION_SCALE`.
 *
 * Absent means not reduced. The element is installed by the platform's window recomposer, so a
 * composition running outside one — a screenshot harness, a test with a bare effect context — gets
 * full motion rather than an exception.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    // The scope is only a handle on the composition's coroutine context, which is where the platform
    // puts the scale. Nothing is launched in it.
    val scope = rememberCoroutineScope()

    return scope.coroutineContext[MotionDurationScale]?.scaleFactor == 0f
}
