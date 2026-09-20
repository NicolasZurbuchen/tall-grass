package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * One clock for a whole screen's entrance, in milliseconds since it began.
 *
 * Runs once per screen and not once per item, and survives the reader leaving and coming back.
 * An item composed after it has finished reads a fraction of 1 and animates nothing.
 *
 * [key] restarts it: pass whatever identifies the content, so switching to a different Pokemon
 * enters again and coming back to the same one does not. [enabled] false starts it finished.
 *
 * DECISIONS.md § One entrance clock per screen, not one animation per item
 */
@Composable
fun rememberEntranceClock(
    key: Any? = Unit,
    enabled: Boolean = true,
): State<Int> {
    var hasRun by rememberSaveable(key) { mutableStateOf(false) }

    val clock = remember(key) { Animatable(if (enabled && !hasRun) 0f else FINISHED) }

    LaunchedEffect(key, enabled) {
        if (!enabled || hasRun) {
            clock.snapTo(FINISHED)
            return@LaunchedEffect
        }

        // Linear on purpose: this is a clock, and each item applies its own easing to its own slice
        // of it. Easing the clock would ease every item twice.
        clock.animateTo(FINISHED, tween(durationMillis = FINISHED.toInt(), easing = LinearEasing))
        hasRun = true
    }

    return remember(clock) { derivedStateOf { clock.value.toInt() } }
}

/**
 * How far through its own entrance the item at [viewportIndex] is, read off a screen clock at
 * [elapsedMillis].
 *
 * [viewportIndex] is the item's position **in the visible viewport**, never its index in the list.
 * See [AppStagger].
 */
fun entranceFraction(
    viewportIndex: Int,
    elapsedMillis: Int,
    durationMillis: Int = AppDuration.MEDIUM,
): Float {
    val started = elapsedMillis - AppStagger.delayFor(viewportIndex)

    return (started.toFloat() / durationMillis).coerceIn(0f, 1f)
}

/** Rises into place from below while fading in. The workhorse: grid cards, sheet content, rows. */
fun Modifier.rise(fraction: Float): Modifier =
    graphicsLayer {
        alpha = fraction
        translationY = (1f - AppEasing.EaseOutQuint.transform(fraction)) * RISE_DISTANCE.toPx()
    }

/** Scales up into place. Chips, pills and type badges, which are too small to travel. */
fun Modifier.pop(fraction: Float): Modifier =
    graphicsLayer {
        alpha = fraction
        val scale = POP_FROM + (1f - POP_FROM) * AppEasing.Emphasized.transform(fraction)
        scaleX = scale
        scaleY = scale
    }

/** Drops in from above. Header text, which the artwork below it rises past. */
fun Modifier.heroUp(fraction: Float): Modifier =
    graphicsLayer {
        alpha = fraction
        translationY = (AppEasing.EaseOutQuint.transform(fraction) - 1f) * HERO_DISTANCE.toPx()
    }

/**
 * Slides in from the trailing edge. Text that trails a heading rather than sitting under it.
 *
 * [layoutDirection] has no default because the modifier cannot read one: `graphicsLayer` is handed a
 * density and nothing else, and a hard-coded rightward slide would enter from the wrong side the day
 * a right-to-left locale ships. Call sites read `LocalLayoutDirection`.
 */
fun Modifier.slideInFromEnd(
    fraction: Float,
    layoutDirection: LayoutDirection,
): Modifier =
    graphicsLayer {
        alpha = fraction
        val travelled = 1f - AppEasing.EaseOutQuint.transform(fraction)
        val sign = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
        translationX = sign * travelled * SIDE_DISTANCE.toPx()
    }

/**
 * A clock reading for content that is simply there — a preview, or a component drawn outside any
 * entrance. Every [entranceFraction] read against it is 1.
 */
val ENTRANCE_DONE: Int = AppStagger.delayFor(AppStagger.MAX_ITEMS) + AppDuration.MEDIUM

// The clock's own length. Anything reading a fraction after this is reading 1.
private val FINISHED = ENTRANCE_DONE.toFloat()

// Far enough to read as arriving, short enough that a card does not cross the one below it.
private val RISE_DISTANCE = 24.dp

private val HERO_DISTANCE = 16.dp

// Further than the vertical entrances travel: sideways movement has the whole width to read against,
// so the same 16dp barely registers.
private val SIDE_DISTANCE = 48.dp

// Not from zero. A pill scaling up from nothing reads as a balloon; from 0.85 it reads as settling.
private const val POP_FROM = 0.85f
