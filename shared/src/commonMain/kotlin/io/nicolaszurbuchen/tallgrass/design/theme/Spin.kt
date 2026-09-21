package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember

/**
 * Degrees, turning once every [AppDuration.LOOP] for as long as this is composed.
 *
 * The `spin` keyframe from #12, and the driver rather than the drawing: the caller owns the thing
 * that turns, which is what lets one of these run several ornaments on a screen. The cost is per
 * transition and not per element (CMP-8146), so a second call is a second 40% rather than a rounding
 * error — a screen wanting two spinning things reads this once and hands the value to both.
 *
 * **Stop it by not composing it.** There is no paused state here; a caller whose ornament has gone
 * off screen leaves this out of the composition, which disposes the transition. See #12 section 6.
 *
 * Linear, because an eased revolution has a visible slow point every five seconds and a pokeball has
 * no top to arrive at.
 */
@Composable
fun rememberSpin(): State<Float> {
    // Held rather than turning. Compose's duration scaling would make each revolution instant, which
    // is a strobe rather than a reduction.
    // DECISIONS.md, Reduced motion is answered per category, not left to the duration scale
    if (rememberReducedMotion()) return remember { mutableFloatStateOf(0f) }

    val transition = rememberInfiniteTransition(label = "spin")

    return transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = AppDuration.LOOP, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "spinDegrees",
    )
}

// Restart rather than Reverse, so the value wraps where the drawing already repeats.
private const val FULL_TURN = 360f
