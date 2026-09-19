package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailArtwork
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailHeader
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailSheetSkeleton
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailTabRow
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.DetailTint
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.FormPillRow
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component.StatsTab
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel

/**
 * The artwork is centred over a sheet that starts halfway up it, which is the layout the imported
 * design is built around. It is laid out that way rather than offset with a z-index: the sheet fills
 * the space under the header and is inset from the top by half the artwork, and the artwork is drawn
 * after it in the same box, so it sits on top without anyone computing a screen height.
 *
 * The tint runs behind the status bar, so this screen takes the insets itself rather than inheriting
 * them from the navigation host: the header clears the status bar and the sheet's content clears the
 * navigation bar, while both backgrounds run to the edge.
 */
@Composable
fun DetailScreen(
    state: DetailUiModel,
    onBackClick: () -> Unit,
    onFormClick: (String) -> Unit,
    onTabClick: (DetailTabUiModel) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keyed on the form, so arriving and switching form both enter, and scrolling does not. A tab
    // change is a crossfade below rather than a second entrance of the whole screen.
    val elapsed by rememberEntranceClock(state.content?.activeFormSlug, enabled = !rememberReducedMotion())

    // The hero colour is the primary type's, and switching form changes it. Animated so the change
    // reads as the same screen becoming something else rather than as a cut.
    val tint by animateColorAsState(
        targetValue = state.tint,
        animationSpec = tween(durationMillis = AppDuration.SHORT, easing = AppEasing.EaseInOut),
        label = "heroTint",
    )

    Column(modifier = modifier.fillMaxSize()) {
        // The tinted band, and the whole of it: the header, plus the strip the artwork hangs over
        // down to the top of the sheet. Sized to exactly what is coloured so the travelling half of
        // it has nothing to disagree with -- a shared layer that covered more would sit over the
        // sheet for the length of the flight, and one that covered less would leave a seam of a
        // second shade while the screen behind it is still fading up.
        Box {
            DetailTint(tint = tint, tintKey = state.tintKey, modifier = Modifier.matchParentSize())

            Column {
                DetailHeader(
                    content = state.content,
                    onBackClick = onBackClick,
                    modifier = Modifier.statusBarsPadding(),
                    elapsedMillis = elapsed,
                )

                // The half of the artwork that hangs below the header. It is held open here rather
                // than as padding on the sheet so that the colour behind it belongs to the band.
                Spacer(modifier = Modifier.height(ARTWORK_SIZE / 2))
            }
        }

        // Tinted too, which is only ever seen in the two rounded shoulders of the sheet. The band
        // above stops at the sheet's top edge on purpose, so this is what colours the corners.
        Box(modifier = Modifier.fillMaxSize().background(tint)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = SHEET_CORNER, topEnd = SHEET_CORNER))
                        .background(MaterialTheme.appColors.surface)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(top = ARTWORK_SIZE / 2 + MaterialTheme.spacing.md)
                        .padding(bottom = MaterialTheme.spacing.xxl),
            ) {
                val content = state.content

                when {
                    state.error != null -> {
                        AppErrorBanner(
                            text = state.error.title,
                            icon = state.error.icon,
                            onRetry = onRetryClick,
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md),
                        )
                    }

                    content == null -> {
                        DetailSheetSkeleton(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md))
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
                                    .padding(horizontal = MaterialTheme.spacing.md)
                                    .rise(entranceFraction(1, elapsed)),
                        )

                        Crossfade(
                            targetState = content.tab,
                            animationSpec = tween(durationMillis = AppDuration.SHORT, easing = AppEasing.EaseInOut),
                            label = "detailTab",
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = MaterialTheme.spacing.md,
                                        end = MaterialTheme.spacing.md,
                                        top = MaterialTheme.spacing.md,
                                    )
                                    .rise(entranceFraction(2, elapsed)),
                        ) { tab ->
                            when (tab) {
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

            DetailArtwork(
                artworkUrl = state.artworkUrl,
                artworkKey = state.artworkKey,
                // Lifted by half its height so it still straddles the sheet's top edge, now that the
                // band below the header holds that space open instead of the sheet.
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = -ARTWORK_SIZE / 2)
                        .size(ARTWORK_SIZE),
            )
        }
    }
}

private val ARTWORK_SIZE = 200.dp
private val SHEET_CORNER = 30.dp
