package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppCollapsingSheet
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppPokeball
import io.nicolaszurbuchen.tallgrass.design.component.AppTabRow
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.arcTopShape
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.rememberEntranceClock
import io.nicolaszurbuchen.tallgrass.design.theme.rememberReducedMotion
import io.nicolaszurbuchen.tallgrass.design.theme.rememberSpin
import io.nicolaszurbuchen.tallgrass.design.theme.rise
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.AboutTab
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailHeader
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailSheetSkeleton
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.FormPillRow
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.HeroCarousel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.MovesTab
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
    onAbilityClick: (String) -> Unit,
    onMoveClick: (String) -> Unit,
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

    // True while a tab tap is animating the pager, which is the one time the pages it crosses are
    // not somewhere the reader has asked to be. See the two effects below.
    var isAnimatingTab by remember { mutableStateOf(false) }

    // Tapping a tab moves the pager, and only when the pager is not already there: a swipe reports
    // its new page before it settles, and animating to the page it just reached fights the finger.
    LaunchedEffect(tab) {
        if (tab != null && tabPagerState.currentPage != tab.ordinal) {
            isAnimatingTab = true
            try {
                tabPagerState.animateScrollToPage(tab.ordinal)
            } finally {
                isAnimatingTab = false
            }
        }
    }

    // Swiping moves the tab row. `currentPage` rather than `settledPage`, so the underline crosses
    // with the finger at the halfway point instead of waiting for the animation to finish.
    //
    // **Silent while the tap above is animating**, which is what makes a tap across two tabs work.
    // About to Moves crosses Base Stats, `currentPage` reports that crossing halfway through, and
    // reporting it selected the middle tab -- which changed `tab`, restarted the effect above and
    // cancelled the animation it was still running. The pager stopped on Base Stats, and the only
    // taps that worked were the ones between neighbours, which cross nothing.
    LaunchedEffect(tabPagerState) {
        snapshotFlow { tabPagerState.currentPage }
            .collect { page -> if (!isAnimatingTab) onTabSelected(DetailTabUiModel.entries[page]) }
    }

    val density = LocalDensity.current
    val dragScope = rememberCoroutineScope()

    // 0 resting over the artwork, 1 up against the toolbar.
    //
    // **A plain float that drags write straight into, not an Animatable snapped from a coroutine.**
    // That indirection is what left the sheet stranded mid-travel, in two ways. A frame carrying
    // several deltas ran `drag` several times, each reading the same not-yet-updated value and each
    // telling the scroll it had consumed its share, while only the last snap actually landed — so the
    // sheet moved less than it claimed and could sit against a finger that was still moving. And a
    // snap still queued when the finger lifted cancelled the settle meant to follow it, because an
    // Animatable serialises its own mutations.
    //
    // Saved rather than remembered, because opening a move or an ability from the Moves tab leaves
    // this screen and coming back rebuilds it. A float saves on its own, where an Animatable needed a
    // Saver to unpick it.
    val expansion = rememberSaveable { mutableFloatStateOf(0f) }
    val progress = expansion.floatValue

    // Front-loaded: the hero is gone in the first quarter of the drag, so the sheet is never rising
    // behind something still solid.
    val heroAlpha = 1f - (progress / HERO_FADE_BY).coerceIn(0f, 1f)

    // The hero measures itself: a status bar, a name, a row of pills, a genus and the artwork, and
    // only the first of those has a number anyone could have written down.
    var heroHeight by remember { mutableStateOf(0.dp) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(tint)) {
        // Drawn before the sheet, so the sheet crops it: the watermark belongs to the hero and ends
        // where the hero does. Everything else about the hero is drawn after the sheet instead --
        // see the Column at the bottom of this box.
        //
        // Composed only while there is a hero to stand on it, which is how the spin stops: an
        // infinite transition has no paused state, and leaving it out of the composition disposes
        // it. See #12 section 6.
        if (heroHeight > 0.dp && heroAlpha > 0f) {
            val spin by rememberSpin()

            AppPokeball(
                color = Color.White.copy(alpha = HERO_BALL_ALPHA),
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        // Centred on the artwork, which is the last thing in the hero column and so
                        // ends where the column does.
                        .offset(y = heroHeight - (ARTWORK_SIZE + HERO_BALL_SIZE) / 2)
                        .requiredSize(HERO_BALL_SIZE)
                        .graphicsLayer {
                            rotationZ = spin
                            alpha = heroAlpha
                        },
            )
        }

        AppCollapsingSheet(
            progress = { expansion.floatValue },
            onProgressChange = { expansion.floatValue = it },
            heroHeight = heroHeight,
            overlap = ARTWORK_OVERLAP,
            // Clears the part of the artwork lying over the sheet, and settles to a gutter of its own
            // once there is no artwork left to clear: the handle needs room under the arc's apex.
            headroom = lerpDp(ARTWORK_OVERLAP, MaterialTheme.spacing.md, progress),
        ) {
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

                    AppTabRow(
                        tabs = DetailTabUiModel.entries.map { it.label },
                        selectedIndex = content.tab.ordinal,
                        onTabClick = { index -> onTabClick(DetailTabUiModel.entries[index]) },
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
                                    AboutTab(about = content.about, tint = tint)
                                }

                                DetailTabUiModel.STATS -> {
                                    StatsTab(stats = content.stats, tint = tint, elapsedMillis = elapsed)
                                }

                                DetailTabUiModel.MOVES -> {
                                    MovesTab(
                                        abilities = content.abilities,
                                        moves = content.moves,
                                        tint = tint,
                                        onAbilityClick = onAbilityClick,
                                        onMoveClick = onMoveClick,
                                    )
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
                        .invisibleWhen(heroAlpha == 0f)
                        .graphicsLayer { alpha = heroAlpha },
            )
        }
    }
}

private val ARTWORK_SIZE = 200.dp

// How much of the artwork the sheet covers. The artwork is square and many Pokemon do not reach the
// bottom of their own frame, so the share of the box that overlaps is always more than the share of
// the drawing: at a tenth the smaller ones floated clear of the sheet altogether.
private val ARTWORK_OVERLAP = ARTWORK_SIZE * 0.33f

// The artwork's own size. The frame is square and most Pokemon do not fill the corners of it, so at
// parity the rim still comes out from behind the drawing on every side without the ball becoming
// the larger of the two objects.
private val HERO_BALL_SIZE = ARTWORK_SIZE

// Heavier than the watermark on a card, because this one is a third covered by the sheet and has no
// text on it to compete with.
private const val HERO_BALL_ALPHA = 0.18f

// The hero is gone in the first quarter of the drag. Front-loaded on purpose: the sheet is what the
// reader is moving, so everything it takes the place of should be gone by the time they have decided
// to move it.
private const val HERO_FADE_BY = 0.25f

// A card standing behind the one in front is in its shadow. Far enough off the ground to read against
// it, close enough that it stays part of it rather than becoming a second colour on the screen.
private const val SILHOUETTE_SHADE = 0.25f

/**
 * Measured, so the space stays; not placed, so nothing is drawn and nothing is hit.
 *
 * `View.INVISIBLE`, which Compose has no single modifier for. Alpha alone draws nothing and still
 * answers a pointer, and that is not a subtlety here: faded out, the hero carousel lies exactly over
 * the expanded sheet's tabs, and a horizontal pager between a finger and a horizontal pager is a
 * gesture that works one time in four.
 *
 * Not placing it rather than not composing it, because the hero's height is what decides where the
 * sheet rests. A carousel that left the layout would take 200dp of that with it and the sheet would
 * jump on the way back down.
 *
 * DECISIONS.md § The sheet expands and the hero becomes a toolbar
 */
private fun Modifier.invisibleWhen(invisible: Boolean): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            if (!invisible) placeable.place(0, 0)
        }
    }
