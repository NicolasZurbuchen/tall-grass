package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppCollapsingSheet
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

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.accent)) {
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

// How far the sheet rides up over the hero at rest, matching a move's. There is no artwork on either
// hero, so this is only enough for the arc to cut into the colour rather than meeting it in a
// straight line.
private val SHEET_OVERLAP = 24.dp

// Three across, which is what a card holding a picture and two short lines wants. The same as a
// move's Learned by grid, because it is the same card with a different second line.
private const val HOLDER_GRID_COLUMNS = 3
