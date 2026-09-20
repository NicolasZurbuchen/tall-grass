package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.arcTopShape
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.rememberEntranceClock
import io.nicolaszurbuchen.tallgrass.design.theme.rememberReducedMotion
import io.nicolaszurbuchen.tallgrass.design.theme.rise
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.AboutTab
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailHeader
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailSheetSkeleton
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailTabRow
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.FormPillRow
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.HeroCarousel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.StatsTab
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.lerp as lerpDp

/**
 * The hero and the sheet are siblings in one box rather than a column, because the sheet slides up
 * over the hero and a column cannot do that. How far it has been dragged is the one piece of state
 * the whole screen reads: it positions the sheet, fades the hero out, and carries the name into the
 * back arrow's row. See `DECISIONS.md § The sheet expands and the hero becomes a toolbar`.
 *
 * The sheet does not scroll as one piece. Its form switcher and tab row are pinned and each tab
 * scrolls inside the pager below them — see `DECISIONS.md § The tabs are a pager, so the sheet stops
 * scrolling as one piece`.
 *
 * The tint runs behind the status bar, so this screen takes the insets itself rather than inheriting
 * them from the navigation host: the header clears the status bar and the sheet's content clears the
 * navigation bar, while both backgrounds run to the edge.
 */
@Composable
fun DetailScreen(
    state: DetailUiModel,
    onBackClick: () -> Unit,
    onEntrySwipe: (String) -> Unit,
    onFormClick: (String) -> Unit,
    onTabClick: (DetailTabUiModel) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Latched: true from the first read onwards and never false again, so the entrance runs when the
    // screen fills and not when a swipe or a form switch refills it.
    // DECISIONS.md § The entrance belongs to the arrival, not to the content
    var hasFilled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.content) { if (state.content != null) hasFilled = true }

    val elapsed by rememberEntranceClock(hasFilled, enabled = !rememberReducedMotion())

    // The hero colour is the card's, and both the carousel and the switcher change it. Animated so
    // the change reads as the same screen becoming something else rather than as a cut.
    val tint by animateColorAsState(
        targetValue = state.tint,
        animationSpec = tween(durationMillis = AppDuration.SHORT, easing = AppEasing.EaseInOut),
        label = "heroTint",
    )

    val heroPagerState = rememberPagerState(pageCount = { state.heroes.size })
    val tabPagerState = rememberPagerState(pageCount = { DetailTabUiModel.entries.size })
    val tab = state.content?.tab
    val onTabSelected by rememberUpdatedState(onTabClick)
    val onEntrySelected by rememberUpdatedState(onEntrySwipe)

    // The carousel opens holding one card, because the list behind it has not been read yet. When it
    // lands the pager is suddenly a thousand pages long and sitting on the wrong one, so it is put
    // back where it belongs without an animation -- there is nothing to animate, the reader has not
    // moved.
    LaunchedEffect(state.activeIndex, state.heroes.size) {
        if (heroPagerState.currentPage != state.activeIndex) {
            heroPagerState.scrollToPage(state.activeIndex)
        }
    }

    // A swipe reports its new card at the halfway point rather than on the settle, so the name, the
    // number, the types and the colour cross with the finger and the read starts while it is still
    // moving.
    LaunchedEffect(heroPagerState, state.heroes) {
        snapshotFlow { heroPagerState.currentPage }
            .collect { page -> state.heroes.getOrNull(page)?.let { onEntrySelected(it.slug) } }
    }

    // Tapping a tab moves the pager, and only when the pager is not already there: a swipe reports
    // its new page before it settles, and animating to the page it just reached fights the finger.
    LaunchedEffect(tab) {
        if (tab != null && tabPagerState.currentPage != tab.ordinal) {
            tabPagerState.animateScrollToPage(tab.ordinal)
        }
    }

    // Swiping moves the tab row. `currentPage` rather than `settledPage`, so the underline crosses
    // with the finger at the halfway point instead of waiting for the animation to finish.
    LaunchedEffect(tabPagerState) {
        snapshotFlow { tabPagerState.currentPage }
            .collect { page -> onTabSelected(DetailTabUiModel.entries[page]) }
    }

    val density = LocalDensity.current
    val dragScope = rememberCoroutineScope()

    // 0 resting over the artwork, 1 up against the toolbar. An Animatable rather than a plain float,
    // so releasing settles the sheet rather than leaving it wherever the finger stopped.
    val expansion = remember { Animatable(0f) }
    val progress = expansion.value

    // Front-loaded: the hero is gone in the first quarter of the drag, so the sheet is never rising
    // behind something still solid.
    val heroAlpha = 1f - (progress / HERO_FADE_BY).coerceIn(0f, 1f)

    // The hero measures itself: a status bar, a name, a row of pills, a genus and the artwork, and
    // only the first of those has a number anyone could have written down.
    var heroHeight by remember { mutableStateOf(0.dp) }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(tint)) {
        val restingTop = (heroHeight - ARTWORK_OVERLAP).coerceAtLeast(0.dp)
        val raisedTop = statusBar + TOOLBAR_HEIGHT
        val travelPx = with(density) { (restingTop - raisedTop).coerceAtLeast(0.dp).toPx() }

        // Scrolling the sheet moves it before it scrolls its content, and only in the direction
        // that has anywhere to go. Dragging up spends the drag on expanding until the sheet is up;
        // dragging down spends it on collapsing, but only what the content underneath did not take,
        // which is what keeps a scrolled tab scrolling rather than pulling the sheet with it.
        // DECISIONS.md § The sheet expands and the hero becomes a toolbar
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
                        if (expansion.value <= 0f || expansion.value >= 1f) return Velocity.Zero

                        expansion.animateTo(
                            settleTarget(expansion.value, available.y),
                            tween(AppDuration.SHORT, easing = AppEasing.EaseOutQuint),
                        )

                        return available
                    }

                    private fun drag(
                        delta: Float,
                        expanding: Boolean,
                    ): Offset {
                        if (travelPx <= 0f) return Offset.Zero
                        if (expanding && delta >= 0f) return Offset.Zero
                        if (!expanding && delta <= 0f) return Offset.Zero

                        val next = (expansion.value - delta / travelPx).coerceIn(0f, 1f)
                        val moved = next - expansion.value
                        if (moved == 0f) return Offset.Zero

                        dragScope.launch { expansion.snapTo(next) }

                        return Offset(0f, -moved * travelPx)
                    }
                }
            }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = lerpDp(restingTop, raisedTop, progress))
                    .nestedScroll(sheetScroll)
                    .clip(arcTopShape(SHEET_ARC))
                    .background(MaterialTheme.appColors.surface)
                    .navigationBarsPadding(),
        ) {
            // Clears the part of the artwork lying over the sheet, and stops clearing it as the
            // artwork fades away.
            Spacer(modifier = Modifier.height(lerpDp(ARTWORK_OVERLAP, 0.dp, progress)))

            SheetHandle(
                onDrag = { delta ->
                    dragScope.launch {
                        val step = if (travelPx > 0f) delta / travelPx else 0f
                        expansion.snapTo((expansion.value - step).coerceIn(0f, 1f))
                    }
                },
                onRelease = { velocity ->
                    expansion.animateTo(
                        settleTarget(expansion.value, velocity),
                        tween(AppDuration.SHORT, easing = AppEasing.EaseOutQuint),
                    )
                },
            )

            val content = state.content

            when {
                state.error != null -> {
                    AppErrorBanner(
                        text = state.error.title,
                        icon = state.error.icon,
                        onRetry = onRetryClick,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                    )
                }

                content == null -> {
                    DetailSheetSkeleton(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg))
                }

                else -> {
                    if (content.forms.isNotEmpty()) {
                        FormPillRow(
                            forms = content.forms,
                            activeSlug = content.activeFormSlug,
                            onFormClick = onFormClick,
                            modifier = Modifier.padding(bottom = MaterialTheme.spacing.md),
                            elapsedMillis = elapsed,
                        )
                    }

                    DetailTabRow(
                        selected = content.tab,
                        onTabClick = onTabClick,
                        modifier =
                            Modifier
                                .padding(horizontal = MaterialTheme.spacing.lg)
                                .rise(entranceFraction(1, elapsed)),
                    )

                    // Full-bleed, so the swipe starts at the screen edge; the gutter is inside each
                    // page instead.
                    HorizontalPager(
                        state = tabPagerState,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .rise(entranceFraction(2, elapsed)),
                    ) { page ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = MaterialTheme.spacing.lg)
                                    .padding(top = MaterialTheme.spacing.md, bottom = MaterialTheme.spacing.xxl),
                        ) {
                            when (DetailTabUiModel.entries[page]) {
                                DetailTabUiModel.ABOUT -> {
                                    AboutTab(about = content.about)
                                }

                                DetailTabUiModel.STATS -> {
                                    StatsTab(stats = content.stats, tint = tint, elapsedMillis = elapsed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Drawn after the sheet, so the Pokemon stands on it rather than behind it. By the time the
        // sheet has risen far enough to reach the header, the hero has already faded away.
        Column(modifier = Modifier.onSizeChanged { heroHeight = with(density) { it.height.toDp() } }) {
            DetailHeader(
                name = state.name,
                numberText = state.numberText,
                types = state.types,
                artworkKey = state.heroes.getOrNull(state.activeIndex)?.artworkKey,
                content = state.content,
                onBackClick = onBackClick,
                onPreviousClick = { dragScope.launch { heroPagerState.animateScrollToPage(state.activeIndex - 1) } },
                onNextClick = { dragScope.launch { heroPagerState.animateScrollToPage(state.activeIndex + 1) } },
                hasPrevious = state.activeIndex > 0,
                hasNext = state.activeIndex < state.heroes.lastIndex,
                modifier = Modifier.statusBarsPadding(),
                elapsedMillis = elapsed,
                collapseProgress = progress,
            )

            HeroCarousel(
                heroes = state.heroes,
                silhouette = lerp(tint, Color.Black, SILHOUETTE_SHADE),
                pagerState = heroPagerState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ARTWORK_SIZE)
                        .graphicsLayer { alpha = heroAlpha },
            )
        }
    }
}

/**
 * The strip the sheet is dragged by, which is not the only part of it that moves it.
 *
 * Scrolling anywhere on the sheet opens it too — see the nested-scroll connection above. The handle
 * stays because that gesture is discoverable only by people already looking for it, and because it
 * is the one target that still works when the tab below has nothing to scroll.
 */
@Composable
private fun SheetHandle(
    onDrag: (Float) -> Unit,
    onRelease: suspend (Float) -> Unit,
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
                    state = rememberDraggableState(onDelta = onDrag),
                    onDragStopped = { velocity -> onRelease(velocity) },
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

private val ARTWORK_SIZE = 200.dp

// How far the sheet's middle sits above its corners. The apex is where the Pokemon stands, so this
// changes how much ground shows at the sides and not how much of the artwork is covered.
private val SHEET_ARC = 32.dp

// How much of the artwork the sheet covers. The artwork is square and many Pokemon do not reach the
// bottom of their own frame, so the share of the box that overlaps is always more than the share of
// the drawing: at a tenth the smaller ones floated clear of the sheet altogether.
private val ARTWORK_OVERLAP = ARTWORK_SIZE * 0.33f

// The hero is gone in the first quarter of the drag. Front-loaded on purpose: the sheet is what the
// reader is moving, so everything it takes the place of should be gone by the time they have decided
// to move it.
private const val HERO_FADE_BY = 0.25f

// A card standing behind the one in front is in its shadow. Far enough off the ground to read against
// it, close enough that it stays part of it rather than becoming a second colour on the screen.
private const val SILHOUETTE_SHADE = 0.25f

// What the header is left as when the sheet is all the way up: the back arrow's row, with the name
// beside it.
private val TOOLBAR_HEIGHT = 56.dp

private val HANDLE_ROW_HEIGHT = 28.dp
private val HANDLE_WIDTH = 36.dp
private val HANDLE_HEIGHT = 4.dp

// Where a release with no flick in it goes.
private const val HALFWAY = 0.5f

// Pixels per second past which the flick decides instead of the position. Low enough that a short
// flick works, high enough that a slow drag goes wherever it was left nearest to.
private const val FLING_VELOCITY = 400f

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
