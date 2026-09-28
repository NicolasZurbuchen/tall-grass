package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.component.AppScreenHeader
import io.nicolaszurbuchen.tallgrass.design.component.AppTabRow
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionAboutTab
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionDetailSkeleton
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionDexCard
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component.RegionLocationRow
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionTabUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.region_detail_no_matches
import tallgrass.shared.generated.resources.region_detail_no_pokedex

/**
 * One region, over three tabs.
 *
 * The same tab idiom as the Pokemon and ability details: a row of labels over a pager, so the tabs
 * can be swiped as well as tapped. All three tabs are loaded before any of them is drawn -- three
 * reads of one local database is one round trip, and a tab that populates a beat after it is tapped
 * reads as slower than one that was always ready.
 *
 * The header carries the region's colour rather than the theme's, which is what ties the three tabs
 * to the card that opened them.
 */
@Composable
fun RegionDetailScreen(
    state: RegionDetailUiModel,
    onTabClick: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onLocationClick: (String) -> Unit,
    onPokemonClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { RegionTabUiModel.entries.size })
    val onTabSelected by rememberUpdatedState(onTabClick)

    // Tapping a tab moves the pager, and only when the pager is not already there: a swipe reports
    // its new page before it settles, and animating to the page it just reached fights the finger.
    LaunchedEffect(state.tab) {
        if (pagerState.currentPage != state.tab.ordinal) {
            pagerState.animateScrollToPage(state.tab.ordinal)
        }
    }

    // Swiping moves the tab row. `settledPage` rather than `currentPage`, unlike the two-tab screens:
    // there are three tabs here, so a tap from the first to the third crosses the second, and
    // reporting it would cancel the animation half way across.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page -> onTabSelected(page) }
    }

    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxWidth().background(state.color ?: MaterialTheme.appColors.surfaceRaised)) {
            AppScreenHeader(title = state.name, onBackClick = onBackClick)

            state.subtitleText?.let { subtitle ->
                Text(
                    text = subtitle.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
                )
            }
        }

        AppTabRow(
            tabs = RegionTabUiModel.entries.map { UiText.Resource(it.label) },
            selectedIndex = state.tab.ordinal,
            onTabClick = onTabClick,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.error != null -> {
                    AppErrorBanner(
                        text = state.error.title,
                        icon = state.error.icon,
                        onRetry = onRetryClick,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.isLoading -> {
                    RegionDetailSkeleton()
                }

                else -> {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        when (RegionTabUiModel.entries[page]) {
                            RegionTabUiModel.ABOUT -> AboutPage(state)
                            RegionTabUiModel.LOCATIONS -> LocationsPage(state, onQueryChange, onLocationClick)
                            RegionTabUiModel.POKEDEX -> PokedexPage(state, onPokemonClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutPage(state: RegionDetailUiModel) {
    val about = state.about ?: return

    LazyColumn(
        contentPadding = PaddingValues(MaterialTheme.spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            RegionAboutTab(
                about = about,
                tint = state.color ?: MaterialTheme.appColors.textPrimary,
            )
        }
    }
}

@Composable
private fun LocationsPage(
    state: RegionDetailUiModel,
    onQueryChange: (String) -> Unit,
    onLocationClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            placeholder = { state.searchHint?.let { Text(it.asString()) } },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        )

        if (state.locations.isEmpty()) {
            Text(
                text = stringResource(Res.string.region_detail_no_matches),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
            )
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = state.locations, key = { it.slug }) { location ->
                RegionLocationRow(
                    location = location,
                    onClick = { onLocationClick(location.slug) },
                )
            }

            // Only while something is typed: a reader who has filtered needs to know how much of the
            // region they are no longer looking at.
            state.matchesText?.let { matches ->
                item {
                    Text(
                        text = matches.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.appColors.textTertiary,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                    )
                }
            }
        }
    }
}

@Composable
private fun PokedexPage(
    state: RegionDetailUiModel,
    onPokemonClick: (String) -> Unit,
) {
    if (state.dex.isEmpty()) {
        // Orre, and only Orre. Upstream has no regional dex for it, and saying so is the answer --
        // showing the national one instead would be inventing a Pokedex the region never had.
        Text(
            text = stringResource(Res.string.region_detail_no_pokedex),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        contentPadding = PaddingValues(MaterialTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = state.dex, key = { it.slug }) { entry ->
            RegionDexCard(entry = entry, onClick = { onPokemonClick(entry.slug) })
        }
    }
}

// The same two columns the main dex grid uses, so a regional dex reads as the dex filtered rather
// than as a different screen.
private const val GRID_COLUMNS = 2
