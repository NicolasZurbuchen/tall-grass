package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.move.presentation.component.DamageClassIcon
import io.nicolaszurbuchen.tallgrass.design.component.AppCollapsingSheet
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppTabRow
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.arcTopShape
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveDetailHeader
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveDetailSkeleton
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveLearnerCard
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveMechanics
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveStats
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveLearnerUiModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_effect
import tallgrass.shared.generated.resources.move_detail_mechanics
import tallgrass.shared.generated.resources.move_detail_no_learners
import androidx.compose.ui.unit.lerp as lerpDp

/**
 * One move: a coloured hero, and a sheet that can be pulled over it.
 *
 * **The same sheet a Pokemon's detail has**, down to the arc across its top, the handle, the
 * nested-scroll that opens it before it scrolls its content, and the name rising into the toolbar as
 * it closes. See `DECISIONS.md § The sheet expands and the hero becomes a toolbar`. A move has no
 * artwork to stand on the arc, so the hero is shorter and the sheet starts higher; everything else
 * behaves identically, which is the point.
 *
 * Two tabs, and a swipe between them. **Details** is what the move is, in the order it is read: the
 * three figures that decide whether to use it, the sentence that says what it does, and the detail
 * behind the sentence. **Learned by** is who can use it — a different question and a different shape.
 */
@Composable
fun MoveDetailScreen(
    state: MoveDetailUiModel,
    onTabClick: (MoveDetailTabUiModel) -> Unit,
    onLearnerClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val tabPagerState = rememberPagerState(pageCount = { MoveDetailTabUiModel.entries.size })
    val onTabSelected by rememberUpdatedState(onTabClick)

    // Tapping a tab moves the pager, and only when the pager is not already there: a swipe reports
    // its new page before it settles, and animating to the page it just reached fights the finger.
    LaunchedEffect(state.tab) {
        if (tabPagerState.currentPage != state.tab.ordinal) {
            tabPagerState.animateScrollToPage(state.tab.ordinal)
        }
    }

    // Swiping moves the tab row. `currentPage` rather than `settledPage`, so the underline crosses
    // with the finger at the halfway point instead of waiting for the animation to finish.
    LaunchedEffect(tabPagerState) {
        snapshotFlow { tabPagerState.currentPage }
            .collect { page -> onTabSelected(MoveDetailTabUiModel.entries[page]) }
    }

    // 0 resting under the hero, 1 up against the toolbar.
    //
    // **A plain float that drags write straight into, not an Animatable snapped from a coroutine.**
    // That indirection is what left the sheet stranded mid-travel: a frame carrying several deltas
    // ran `drag` several times, each reading the same not-yet-updated value and each telling the
    // scroll it had consumed its share while only the last snap landed, and a snap still queued when
    // the finger lifted cancelled the settle meant to follow it.
    //
    // Saved rather than remembered: this screen can be left for a Pokemon and come back, and a plain
    // remember dropped the sheet to the bottom of the hero on the way back. A float saves on its own.
    val expansion = rememberSaveable { mutableFloatStateOf(0f) }
    val progress = expansion.floatValue

    // The hero measures itself: a status bar, a name and a pill, and only the first of those has a
    // number anyone could have written down.
    var heroHeight by remember { mutableStateOf(0.dp) }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val tint = state.move?.type?.color ?: MaterialTheme.appColors.surfaceRaised

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(tint)) {
        // Drawn before the sheet, so the sheet crops it: the glyph belongs to the hero and ends where
        // the hero does. It is what a Pokemon's hero puts its artwork in, and the reason this screen
        // stopped feeling like an empty coloured block.
        if (heroHeight > 0.dp && state.move != null) {
            DamageClassIcon(
                damageClass = state.move.damageClass,
                color = Color.White.copy(alpha = GLYPH_ALPHA),
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = GLYPH_CROP)
                        .requiredWidth(GLYPH_WIDTH)
                        .graphicsLayer { alpha = 1f - (progress / HERO_FADE_BY).coerceIn(0f, 1f) },
            )
        }

        AppCollapsingSheet(
            progress = { expansion.floatValue },
            onProgressChange = { expansion.floatValue = it },
            heroHeight = heroHeight,
            overlap = SHEET_OVERLAP,
            // Room under the arc's apex for the handle, and the same at every position: there is no
            // artwork here for the sheet to be clearing as it rises.
            headroom = MaterialTheme.spacing.md,
        ) {
            when {
                state.error != null -> {
                    AppErrorBanner(
                        text = state.error.title,
                        icon = state.error.icon,
                        onRetry = onRetryClick,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                    )
                }

                state.move == null -> {
                    ShimmerPulse {
                        MoveDetailSkeleton(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg))
                    }
                }

                else -> {
                    AppTabRow(
                        tabs = MoveDetailTabUiModel.entries.map { it.label },
                        selectedIndex = state.tab.ordinal,
                        onTabClick = { index -> onTabClick(MoveDetailTabUiModel.entries[index]) },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                    )

                    // Full-bleed, so the swipe starts at the screen edge; the gutter is inside each
                    // page instead.
                    HorizontalPager(
                        state = tabPagerState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    ) { page ->
                        when (MoveDetailTabUiModel.entries[page]) {
                            MoveDetailTabUiModel.DETAILS -> {
                                DetailsTab(move = state.move)
                            }

                            MoveDetailTabUiModel.LEARNERS -> {
                                LearnersTab(learners = state.learners, onLearnerClick = onLearnerClick)
                            }
                        }
                    }
                }
            }
        }

        // Drawn after the sheet, so the name sits over it once the sheet has risen past where the
        // name used to be. By then the rest of the hero has already faded away.
        if (state.move != null) {
            Column(modifier = Modifier.onSizeChanged { heroHeight = with(density) { it.height.toDp() } }) {
                MoveDetailHeader(
                    move = state.move,
                    onBackClick = onBackClick,
                    modifier = Modifier.statusBarsPadding(),
                    collapseProgress = progress,
                )
            }
        }
    }
}

/**
 * What the move is, in the order it is read.
 *
 * A scrolling column rather than a lazy one: the longest this gets is three figures, a paragraph and
 * about ten rows.
 */
@Composable
private fun DetailsTab(
    move: MoveContentUiModel,
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
        MoveStats(stats = move.stats)

        move.effect?.let { effect ->
            SectionTitle(title = Res.string.move_detail_effect)
            Text(
                text = effect,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.appColors.textPrimary,
            )
        }

        SectionTitle(title = Res.string.move_detail_mechanics)
        MoveMechanics(facts = move.facts)
    }
}

/**
 * Who can use it.
 *
 * **A lazy grid rather than the scrolling column the other tab uses**, because the other tab has ten
 * rows and this one has up to 1,213: Rest is learned by that many Pokemon, each with a picture to
 * fetch. Its own scroll container rather than one shared with Details, which is what lets each tab
 * remember where it was.
 */
@Composable
private fun LearnersTab(
    learners: List<MoveLearnerUiModel>,
    onLearnerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (learners.isEmpty()) {
        // The 106 moves nobody is taught. Said out loud, because an empty tab reads as a read that
        // has not finished rather than as an answer.
        Text(
            text = stringResource(Res.string.move_detail_no_learners),
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
        columns = GridCells.Fixed(LEARNER_GRID_COLUMNS),
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
        items(items = learners, key = { it.slug }) { learner ->
            MoveLearnerCard(learner = learner, onClick = { onLearnerClick(learner.slug) })
        }
    }
}

/** The same heading the Pokemon detail's tabs use, so a section reads the same on both screens. */
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

// How far the sheet rides up over the hero at rest. A Pokemon's sheet covers a third of the artwork;
// there is no artwork here, so this is only enough for the arc to cut into the colour rather than
// meeting it in a straight line.
//
// The hero pays for it twice over in its own bottom padding, which is what leaves air between the
// type pill and the arc. At parity the two touched, and a pill sitting on the sheet's edge read as
// the sheet having been dragged up rather than as where it rests.
private val SHEET_OVERLAP = 24.dp

// Taller than the hero, so the glyph runs off both the top and the bottom of it and reads as
// something the screen is a window onto rather than as a picture placed in it.
private val GLYPH_WIDTH = 320.dp
private val GLYPH_CROP = GLYPH_WIDTH * 0.2f

// Fainter than a card's. It is spread over much more of the screen here, and the name sits on it.
private const val GLYPH_ALPHA = 0.16f

// The hero is gone in the first quarter of the drag.
private const val HERO_FADE_BY = 0.25f

// Three across, which is what a card holding a picture and two short lines wants.
private const val LEARNER_GRID_COLUMNS = 3
