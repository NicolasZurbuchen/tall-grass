package io.nicolaszurbuchen.tallgrass.design.component

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.arcTopShape
import androidx.compose.ui.unit.lerp as lerpDp

/**
 * A sheet that rests over a hero and can be pulled up to cover it.
 *
 * Scrolling the sheet moves it before it scrolls its content, and only in the direction that has
 * anywhere to go. Dragging up spends the drag on expanding until the sheet is up; dragging down
 * spends it on collapsing, but only what the content underneath did not take — which is what keeps a
 * scrolled tab scrolling rather than pulling the sheet with it. The handle does the same thing for
 * readers who reach for it instead.
 *
 * DECISIONS.md § The sheet expands and the hero becomes a toolbar
 *
 * **Extracted at the third screen, after the fourth bug.** The Pokemon, move and ability details all
 * draw this, and every fix had to be made three times by hand — twice for the same gesture defect,
 * once corrupting two of the files on the way. That is well past the point where three copies reads
 * as a coincidence.
 *
 * [progress] is 0 at rest and 1 against the toolbar, and it is **read through a lambda rather than
 * taken as a value**: one frame can carry several drag callbacks, and a value parameter would still
 * hold the figure from the last recomposition on the second of them. That is the bug this component
 * was extracted in the middle of — the sheet reported moving further than it had, and lagged the
 * finger it claimed to be following.
 *
 * The caller owns the state, as `rememberSaveable { mutableFloatStateOf(0f) }`, because a screen
 * wants the sheet back where the reader left it after navigating away and back — and because the
 * hero above the sheet fades against the same number.
 *
 * [heroHeight] is what the caller measured its hero to be and [overlap] is how far the sheet rides up
 * over it at rest. [headroom] is the space above the handle, which a caller may vary as the sheet
 * rises: the Pokemon detail shrinks it as the artwork it was clearing goes away.
 */
@Composable
fun AppCollapsingSheet(
    progress: () -> Float,
    onProgressChange: (Float) -> Unit,
    heroHeight: Dp,
    overlap: Dp,
    headroom: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // The gesture callbacks below outlive the recomposition that built them, so they read the
    // current lambdas rather than the ones they were created with.
    val currentProgress by rememberUpdatedState(progress)
    val currentOnChange by rememberUpdatedState(onProgressChange)

    val restingTop = (heroHeight - overlap).coerceAtLeast(0.dp)
    val raisedTop = statusBar + TOOLBAR_HEIGHT
    val travelPx = with(density) { (restingTop - raisedTop).coerceAtLeast(0.dp).toPx() }

    val sheetScroll =
        remember(travelPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset = drag(available.y, expanding = true)

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset = drag(available.y, expanding = false)

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (currentProgress() <= 0f || currentProgress() >= 1f) return Velocity.Zero

                    settleSheet(currentProgress, currentOnChange, available.y)

                    return available
                }

                // **Writes before it answers**, so what it reports consuming is what the sheet has
                // already moved. It used to launch the write into a coroutine and report a move it
                // had not made yet, which left the sheet lagging the finger and let a queued write
                // cancel the settle that came after it.
                private fun drag(
                    delta: Float,
                    expanding: Boolean,
                ): Offset {
                    if (travelPx <= 0f) return Offset.Zero
                    if (expanding && delta >= 0f) return Offset.Zero
                    if (!expanding && delta <= 0f) return Offset.Zero

                    val from = currentProgress()
                    val next = (from - delta / travelPx).coerceIn(0f, 1f)
                    val moved = next - from
                    if (moved == 0f) return Offset.Zero

                    currentOnChange(next)

                    return Offset(0f, -moved * travelPx)
                }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(top = lerpDp(restingTop, raisedTop, progress()))
                .nestedScroll(sheetScroll)
                .clip(arcTopShape(SHEET_ARC))
                .background(MaterialTheme.appColors.surface)
                .navigationBarsPadding(),
    ) {
        Spacer(modifier = Modifier.height(headroom))

        SheetHandle(
            progress = currentProgress,
            onProgressChange = currentOnChange,
            travelPx = travelPx,
        )

        content()
    }
}

/**
 * The strip the sheet is dragged by, which is not the only part of it that moves it.
 *
 * Scrolling anywhere on the sheet opens it too — see the nested-scroll connection above. The handle
 * stays because that gesture is discoverable only by people already looking for it, and because it is
 * the one target that still works when the content below has nothing to scroll.
 */
@Composable
private fun SheetHandle(
    progress: () -> Float,
    onProgressChange: (Float) -> Unit,
    travelPx: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .height(HANDLE_ROW_HEIGHT)
                .draggable(
                    orientation = Orientation.Vertical,
                    state =
                        rememberDraggableState { delta ->
                            val step = if (travelPx > 0f) delta / travelPx else 0f
                            onProgressChange((progress() - step).coerceIn(0f, 1f))
                        },
                    onDragStopped = { velocity -> settleSheet(progress, onProgressChange, velocity) },
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = HANDLE_WIDTH, height = HANDLE_HEIGHT)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.appColors.borderDefault),
        )
    }
}

/**
 * Runs the sheet to whichever end the gesture that just ended was heading for.
 *
 * **The only animation the sheet has, and it runs in the coroutine of the gesture that ended.** That
 * is what makes it safe: nothing writes the position asynchronously, so the only thing that can
 * interrupt this is the reader taking hold of the sheet again, which is what should interrupt it.
 */
private suspend fun settleSheet(
    progress: () -> Float,
    onProgressChange: (Float) -> Unit,
    velocity: Float,
) {
    animate(
        initialValue = progress(),
        targetValue = settleTarget(progress(), velocity),
        animationSpec = tween(AppDuration.SHORT, easing = AppEasing.EaseOutQuint),
    ) { value, _ -> onProgressChange(value) }
}

/**
 * Where a drag or a fling leaves the sheet.
 *
 * A flick decides on its own, whichever end it was nearer: releasing a short upward flick from a
 * sheet barely off its rest still opens it, which is what a flick means. Without one, the sheet goes
 * to whichever end it is closer to.
 */
private fun settleTarget(
    progress: Float,
    velocity: Float,
): Float =
    when {
        velocity < -FLING_VELOCITY -> 1f
        velocity > FLING_VELOCITY -> 0f
        else -> if (progress > HALFWAY) 1f else 0f
    }

// How far the sheet's middle sits above its corners.
private val SHEET_ARC = 32.dp

// What the header above is left as when the sheet is all the way up: a back arrow's row, with the
// screen's name in the middle of it. The sheet stops under that rather than over it.
private val TOOLBAR_HEIGHT = 56.dp

private val HANDLE_ROW_HEIGHT = 28.dp
private val HANDLE_WIDTH = 36.dp
private val HANDLE_HEIGHT = 4.dp

// Where a release with no flick in it goes.
private const val HALFWAY = 0.5f

// Pixels per second past which the flick decides instead of the position. Low enough that a short
// flick works, high enough that a slow drag goes wherever it was left nearest to.
private const val FLING_VELOCITY = 400f
