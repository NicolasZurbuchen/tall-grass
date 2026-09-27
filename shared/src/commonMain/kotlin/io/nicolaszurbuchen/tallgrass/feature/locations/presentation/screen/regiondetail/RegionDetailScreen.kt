package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppCollapsingSheet
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppTabRow
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionAboutTab
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionDetailHeader
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionDetailSkeleton
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionDexCard
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionLocationRow
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One region, over three tabs, on the same collapsing sheet the Pokemon, move and ability details use.
 *
 * The hero carries the region's colour and the sheet rides up over it, which is what ties the three
 * tabs to the card that opened them -- and what makes this read as a sibling of the other three
 * details rather than as a page with a coloured strip on top.
 *
 * **There is no search on the Locations tab.** The first attempt lost focus on every keystroke: the
 * field sat inside a pager page that recomposes whole each time the query changes. A field that cannot
 * be typed into is worse than a long list, so it is gone until it can be built properly.
 */
@Composable
fun RegionDetailScreen(
    state: RegionDetailUiModel,
    onTabClick: (Int) -> Unit,
    onLocationClick: (String) -> Unit,
    onPokemonClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val pagerState = rememberPagerState(pageCount = { RegionTabUiModel.entries.size })
    val onTabSelected by rememberUpdatedState(onTabClick)

    // Tapping a tab moves the pager, and only when the pager is not already there: a swipe reports its
    // new page before it settles, and animating to the page it just reached fights the finger.
    LaunchedEffect(state.tab) {
        if (pagerState.currentPage != state.tab.ordinal) {
            pagerState.animateScrollToPage(state.tab.ordinal)
        }
    }

    // `settledPage` rather than `currentPage`, unlike the two-tab screens: there are three tabs here,
    // so a tap from the first to the third crosses the second, and reporting it mid-flight would
    // cancel the animation half way across.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page -> onTabSelected(page) }
    }

    // 0 resting under the hero, 1 up against the toolbar. A plain saved float rather than an
    // Animatable, for the reason the Pokemon detail records: a snap queued from a coroutine left the
    // sheet stranded mid-travel.
    val expansion = rememberSaveable { mutableFloatStateOf(0f) }
    val progress = expansion.floatValue

    // The hero measures itself: a status bar and three lines of text, and only the first of those has
    // a number anyone could have written down.
    var heroHeight by remember { mutableStateOf(0.dp) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(state.color ?: MaterialTheme.appColors.accent),
    ) {
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

                state.isLoading -> {
                    RegionDetailSkeleton(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg))
                }

                else -> {
                    AppTabRow(
                        tabs = RegionTabUiModel.entries.map { UiText.Resource(it.label) },
                        selectedIndex = state.tab.ordinal,
                        onTabClick = onTabClick,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                    )

                    // Full-bleed, so the swipe starts at the screen edge; the gutter is inside each
                    // page instead.
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    ) { page ->
                        when (RegionTabUiModel.entries[page]) {
                            RegionTabUiModel.ABOUT -> AboutPage(state)
                            RegionTabUiModel.LOCATIONS -> LocationsPage(state, onLocationClick)
                            RegionTabUiModel.POKEDEX -> PokedexPage(state, onPokemonClick)
                        }
                    }
                }
            }
        }

        // Drawn after the sheet, so the toolbar title sits over it once the sheet has risen past where
        // the hero used to be.
        if (!state.isLoading && state.error == null) {
            Column(modifier = Modifier.onSizeChanged { heroHeight = with(density) { it.height.toDp() } }) {
                RegionDetailHeader(
                    name = state.name,
                    nativeName = state.nativeName,
                    subtitle = state.subtitleText,
                    onBackClick = onBackClick,
                    modifier = Modifier.statusBarsPadding(),
                    collapseProgress = progress,
                )
            }
        }
    }
}

@Composable
private fun AboutPage(state: RegionDetailUiModel) {
    val about = state.about ?: return

    LazyColumn(contentPadding = pagePadding(), modifier = Modifier.fillMaxSize()) {
        item {
            RegionAboutTab(
                about = about,
                tint = state.color ?: MaterialTheme.appColors.textPrimary,
            )
        }
    }
}

/**
 * Every place in the region, by name.
 *
 * Its own scroll container rather than one shared with the other tabs, which is what lets each tab
 * remember where it was -- the same reason the ability detail gives its two tabs one each.
 */
@Composable
private fun LocationsPage(
    state: RegionDetailUiModel,
    onLocationClick: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = pagePadding(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = state.locations, key = { it.slug }) { location ->
            RegionLocationRow(location = location, onClick = { onLocationClick(location.slug) })
        }
    }
}

@Composable
private fun PokedexPage(
    state: RegionDetailUiModel,
    onPokemonClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        contentPadding = pagePadding(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = state.dex, key = { it.slug }) { entry ->
            RegionDexCard(entry = entry, onClick = { onPokemonClick(entry.slug) })
        }
    }
}

/**
 * The gutter every page carries, since the pager itself is full-bleed so the swipe starts at the edge.
 *
 * The bottom is deep enough that the last row clears the navigation bar with the sheet fully raised.
 */
@Composable
private fun pagePadding(): PaddingValues =
    PaddingValues(
        start = MaterialTheme.spacing.lg,
        end = MaterialTheme.spacing.lg,
        top = MaterialTheme.spacing.md,
        bottom = MaterialTheme.spacing.xxl,
    )

// How far the sheet rides up over the hero at rest, matching a move's and an ability's. There is no
// artwork on this hero either, so this is only enough for the arc to cut into the colour rather than
// meeting it in a straight line.
private val SHEET_OVERLAP = 24.dp

// The same two columns the main dex grid uses, so a regional dex reads as the dex filtered rather than
// as a different screen.
private const val GRID_COLUMNS = 2
