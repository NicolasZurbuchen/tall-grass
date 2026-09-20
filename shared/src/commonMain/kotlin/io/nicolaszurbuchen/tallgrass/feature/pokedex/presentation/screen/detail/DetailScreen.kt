package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.theme.AppDuration
import io.nicolaszurbuchen.tallgrass.design.theme.AppEasing
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
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

/**
 * The carousel is centred over a sheet whose top edge crosses the Pokemon's feet. It is laid out
 * that way rather than offset with a z-index: the sheet fills the space under the header, inset from
 * the top by all but the overlapping third of the artwork, and the carousel is drawn after it in the
 * same box so it sits on top without anyone computing a screen height.
 *
 * The sheet itself does not scroll. Its form switcher and tab row are pinned and each tab scrolls
 * inside the pager below them — see `DECISIONS.md § The tabs are a pager, so the sheet stops
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
    // Keyed on the content arriving rather than on which form is showing, so the entrance runs
    // once when the read lands, and scrolling, switching form and changing tab all leave it alone.
    // DECISIONS.md § Switching form is a change of content, not a second arrival
    val elapsed by rememberEntranceClock(state.content != null, enabled = !rememberReducedMotion())

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

    Column(modifier = modifier.fillMaxSize().background(tint)) {
        DetailHeader(
            name = state.name,
            numberText = state.numberText,
            types = state.types,
            artworkKey = state.heroes.getOrNull(state.activeIndex)?.artworkKey,
            content = state.content,
            onBackClick = onBackClick,
            modifier = Modifier.statusBarsPadding(),
            elapsedMillis = elapsed,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = ARTWORK_SIZE - ARTWORK_OVERLAP)
                        .clip(RoundedCornerShape(topStart = SHEET_CORNER, topEnd = SHEET_CORNER))
                        .background(MaterialTheme.appColors.surface)
                        .navigationBarsPadding()
                        .padding(top = ARTWORK_OVERLAP + MaterialTheme.spacing.md),
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

                        DetailTabRow(
                            selected = content.tab,
                            onTabClick = onTabClick,
                            modifier =
                                Modifier
                                    .padding(horizontal = MaterialTheme.spacing.lg)
                                    .rise(entranceFraction(1, elapsed)),
                        )

                        // Full-bleed, so the swipe starts at the screen edge; the gutter is inside
                        // each page instead.
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

            HeroCarousel(
                heroes = state.heroes,
                // Lighter than the ground rather than darker: the artwork is drawn over a saturated
                // colour, and a darker flat shape reads as a hole in it.
                silhouette = lerp(tint, Color.White, SILHOUETTE_LIFT),
                pagerState = heroPagerState,
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(ARTWORK_SIZE),
            )
        }
    }
}

private val ARTWORK_SIZE = 200.dp
private val SHEET_CORNER = 30.dp

// How much of the artwork the sheet covers. The artwork is square and many Pokemon do not reach the
// bottom of their own frame, so the share of the box that overlaps is always more than the share of
// the drawing: at a tenth the smaller ones floated clear of the sheet altogether.
private val ARTWORK_OVERLAP = ARTWORK_SIZE * 0.33f

// Far enough off the ground for a silhouette to read against it, close enough that it stays part of
// it rather than becoming a second colour on the screen.
private const val SILHOUETTE_LIFT = 0.22f
