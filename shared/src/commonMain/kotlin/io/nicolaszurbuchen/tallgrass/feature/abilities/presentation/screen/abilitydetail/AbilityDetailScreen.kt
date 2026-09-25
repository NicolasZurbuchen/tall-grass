package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppTabRow
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.arcTopShape
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.component.AbilityDetailHeader
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.component.AbilityDetailSkeleton
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.component.AbilityHolderCard
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityHolderUiModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.ability_detail_effect
import tallgrass.shared.generated.resources.ability_detail_in_depth
import tallgrass.shared.generated.resources.ability_detail_no_holders
import androidx.compose.ui.unit.lerp as lerpDp

/**
 * One ability: a coloured hero, and a sheet that can be pulled over it.
 *
 * **The same sheet a Pokemon's detail and a move's detail have**, down to the arc across its top, the
 * handle, the nested-scroll that opens it before it scrolls its content, and the name rising into the
 * toolbar as it closes. See `DECISIONS.md § The sheet expands and the hero becomes a toolbar`.
 *
 * **The hero is the accent, on the first frame and every frame after.** A move's hero waits for its
 * type before it has a colour, and opens on the neutral surface until then; an ability has no colour
 * of its own to wait for, so there is nothing to find out and no reason to start grey.
 *
 * Two tabs, and a swipe between them. **Details** is what the ability does: the sentence, and the
 * paragraph behind the sentence. **Known by** is who has it — a different question and a different
 * shape.
 */
@Composable
fun AbilityDetailScreen(
    state: AbilityDetailUiModel,
    onTabClick: (AbilityDetailTabUiModel) -> Unit,
    onHolderClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val dragScope = rememberCoroutineScope()

    val tabPagerState = rememberPagerState(pageCount = { AbilityDetailTabUiModel.entries.size })
    val onTabSelected by rememberUpdatedState(onTabClick)

    // Tapping a tab moves the pager, and only when the pager is not already there: a swipe reports
    // its new page before it settles, and animating to the page it just reached fights the finger.
    //
    // There are two tabs, so a tap can only ever move one page and crosses nothing on the way. The
    // Pokemon detail has three and needed a guard against reporting the tab it passes through, which
    // cancelled this effect mid-animation — if a third tab lands here, that guard comes with it.
    LaunchedEffect(state.tab) {
        if (tabPagerState.currentPage != state.tab.ordinal) {
            tabPagerState.animateScrollToPage(state.tab.ordinal)
        }
    }

    // Swiping moves the tab row. `currentPage` rather than `settledPage`, so the underline crosses
    // with the finger at the halfway point instead of waiting for the animation to finish.
    LaunchedEffect(tabPagerState) {
        snapshotFlow { tabPagerState.currentPage }
            .collect { page -> onTabSelected(AbilityDetailTabUiModel.entries[page]) }
    }

    // 0 resting under the hero, 1 up against the toolbar. An Animatable rather than a plain float, so
    // releasing settles the sheet rather than leaving it wherever the finger stopped.
    // Saved rather than remembered: this screen can be left for a Pokemon and come back, and a plain
    // remember dropped the sheet to the bottom of the hero on the way back.
    val expansion = rememberSaveable(saver = SheetExpansionSaver) { Animatable(0f) }
    val progress = expansion.value

    // The hero measures itself: a status bar, a name and a pill, and only the first of those has a
    // number anyone could have written down.
    var heroHeight by remember { mutableStateOf(0.dp) }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.accent)) {
        val restingTop = (heroHeight - SHEET_OVERLAP).coerceAtLeast(0.dp)
        val raisedTop = statusBar + TOOLBAR_HEIGHT
        val travelPx = with(density) { (restingTop - raisedTop).coerceAtLeast(0.dp).toPx() }

        // Scrolling the sheet moves it before it scrolls its content, and only in the direction that
        // has anywhere to go. Dragging up spends the drag on expanding until the sheet is up;
        // dragging down spends it on collapsing, but only what the content underneath did not take,
        // which is what keeps a scrolled tab scrolling rather than pulling the sheet with it.
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
            // Room under the arc's apex for the handle.
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

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

            when {
                state.error != null -> {
                    AppErrorBanner(
                        text = state.error.title,
                        icon = state.error.icon,
                        onRetry = onRetryClick,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                    )
                }

                state.ability == null -> {
                    ShimmerPulse {
                        AbilityDetailSkeleton(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg))
                    }
                }

                else -> {
                    AppTabRow(
                        tabs = AbilityDetailTabUiModel.entries.map { it.label },
                        selectedIndex = state.tab.ordinal,
                        onTabClick = { index -> onTabClick(AbilityDetailTabUiModel.entries[index]) },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                    )

                    // Full-bleed, so the swipe starts at the screen edge; the gutter is inside each
                    // page instead.
                    HorizontalPager(
                        state = tabPagerState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    ) { page ->
                        when (AbilityDetailTabUiModel.entries[page]) {
                            AbilityDetailTabUiModel.DETAILS -> {
                                DetailsTab(ability = state.ability)
                            }

                            AbilityDetailTabUiModel.HOLDERS -> {
                                HoldersTab(holders = state.holders, onHolderClick = onHolderClick)
                            }
                        }
                    }
                }
            }
        }

        // Drawn after the sheet, so the name sits over it once the sheet has risen past where the
        // name used to be. By then the rest of the hero has already faded away.
        if (state.ability != null) {
            Column(modifier = Modifier.onSizeChanged { heroHeight = with(density) { it.height.toDp() } }) {
                AbilityDetailHeader(
                    ability = state.ability,
                    onBackClick = onBackClick,
                    modifier = Modifier.statusBarsPadding(),
                    collapseProgress = progress,
                )
            }
        }
    }
}

/**
 * What the ability is, in the order it is read.
 *
 * A scrolling column rather than a lazy one: the longest this gets is two paragraphs, the second of
 * which runs to 1,555 characters for exactly one ability.
 */
@Composable
private fun DetailsTab(
    ability: AbilityContentUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.lg)
                .padding(top = MaterialTheme.spacing.md, bottom = MaterialTheme.spacing.xxl),
    ) {
        SectionTitle(title = Res.string.ability_detail_effect)
        Text(
            text = ability.shortEffect,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.appColors.textPrimary,
        )

        // Absent for the 46 whose long entry says exactly what the short one did. A heading over a
        // repeat of the line above it reads as a rendering fault rather than as a short answer.
        ability.effect?.let { effect ->
            SectionTitle(title = Res.string.ability_detail_in_depth)
            Text(
                text = effect,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.appColors.textSecondary,
            )
        }
    }
}

/**
 * Who has it.
 *
 * **A lazy grid rather than the scrolling column the other tab uses**, because the other tab has two
 * paragraphs and this one has up to 96, each with a picture to fetch. Its own scroll container rather
 * than one shared with Details, which is what lets each tab remember where it was.
 */
@Composable
private fun HoldersTab(
    holders: List<AbilityHolderUiModel>,
    onHolderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (holders.isEmpty()) {
        // No ability in the dataset is on nothing today, so this is here for a dataset that changes
        // rather than for a case anyone will see. Said out loud all the same, because an empty tab
        // reads as a read that has not finished rather than as an answer.
        Text(
            text = stringResource(Res.string.ability_detail_no_holders),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.appColors.textSecondary,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.lg)
                    .padding(top = MaterialTheme.spacing.md),
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(HOLDER_GRID_COLUMNS),
        contentPadding =
            PaddingValues(
                start = MaterialTheme.spacing.lg,
                end = MaterialTheme.spacing.lg,
                top = MaterialTheme.spacing.md,
                bottom = MaterialTheme.spacing.xxl,
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = modifier.fillMaxSize(),
    ) {
        items(items = holders, key = { it.slug }) { holder ->
            AbilityHolderCard(holder = holder, onClick = { onHolderClick(holder.slug) })
        }
    }
}

/** The same heading the other two details draw, so a section reads the same on all three screens. */
@Composable
private fun SectionTitle(
    title: StringResource,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.appColors.textPrimary,
        modifier = modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.sm),
    )
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

// How far the sheet's middle sits above its corners, matching the other two details.
private val SHEET_ARC = 32.dp

// How far the sheet rides up over the hero at rest, matching a move's. There is no artwork on either
// hero, so this is only enough for the arc to cut into the colour rather than meeting it in a
// straight line.
private val SHEET_OVERLAP = 24.dp

// What the header is left as when the sheet is all the way up: the back arrow's row, with the name
// in the middle of it.
private val TOOLBAR_HEIGHT = 56.dp

private val HANDLE_ROW_HEIGHT = 28.dp
private val HANDLE_WIDTH = 36.dp
private val HANDLE_HEIGHT = 4.dp

// Three across, which is what a card holding a picture and two short lines wants. The same as a
// move's Learned by grid, because it is the same card with a different second line.
private const val HOLDER_GRID_COLUMNS = 3

// Where a release with no flick in it goes.
private const val HALFWAY = 0.5f

// Pixels per second past which the flick decides instead of the position.
private const val FLING_VELOCITY = 400f

/**
 * An `Animatable` cannot be saved, and the one number inside it is the whole of the sheet's position.
 *
 * Restored into a settled `Animatable` rather than an animation in flight: a sheet that was mid-drag
 * when the screen was left should come back where it was let go, not finish a gesture the reader has
 * long since forgotten making.
 */
private val SheetExpansionSaver: Saver<Animatable<Float, AnimationVector1D>, Float> =
    Saver(save = { it.value }, restore = { Animatable(it) })
