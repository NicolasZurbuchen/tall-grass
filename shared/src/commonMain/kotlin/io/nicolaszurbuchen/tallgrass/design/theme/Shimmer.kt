package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Drives every [shimmerBlock] below it from one animated alpha, so a screen's placeholders breathe
 * together. Wrap the skeleton, not the screen — the transition runs for as long as it is composed.
 *
 * See DECISIONS.md § A screen waits as its own silhouette.
 */
@Composable
fun ShimmerPulse(content: @Composable () -> Unit) {
    // A loop, and #12's policy is that decorative loops stop entirely under reduced motion rather
    // than running at zero duration -- which for an infinite repeat means flickering between the two
    // alphas as fast as the display allows, the single worst thing to show someone who asked for
    // less movement. The blocks still draw; they simply hold still.
    if (rememberReducedMotion()) {
        CompositionLocalProvider(LocalShimmerAlpha provides MAX_ALPHA, content = content)
        return
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = MIN_ALPHA,
        targetValue = MAX_ALPHA,
        animationSpec = infiniteRepeatable(animation = tween(PULSE_MILLIS), repeatMode = RepeatMode.Reverse),
        label = "shimmerAlpha",
    )

    CompositionLocalProvider(LocalShimmerAlpha provides alpha, content = content)
}

/**
 * Paints one placeholder. Size it at the call site to match the thing it stands in for — a
 * placeholder of the wrong size is what makes the real content visibly jump when it lands.
 */
@Composable
fun Modifier.shimmerBlock(shape: Shape = RoundedCornerShape(BLOCK_CORNER)): Modifier =
    clip(shape).background(MaterialTheme.appColors.textTertiary.copy(alpha = LocalShimmerAlpha.current))

// Defaulted rather than absent, so a preview or a screenshot test with no ShimmerPulse above it
// still draws the frame — it simply does not breathe.
private val LocalShimmerAlpha = compositionLocalOf { MIN_ALPHA }

// Never fully transparent and never fully opaque: at 0 the blocks disappear and the layout reads as
// empty rather than as loading, and at 1 a tertiary-coloured bar is mistakable for real text.
private const val MIN_ALPHA = 0.10f
private const val MAX_ALPHA = 0.28f

// Slower than the 700ms a list of network results would use. This also covers a cache read that is
// over in a frame or two, and a fast pulse on a placeholder nobody sees for long reads as flicker.
private const val PULSE_MILLIS = 1000

// Softer than the app's smallest shape token, which is sized for cards. A placeholder standing in
// for a line of text wants the corner of a text run, not of a container.
private val BLOCK_CORNER = 4.dp
