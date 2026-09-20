package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailHeroUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedElementOrNone
import kotlin.math.absoluteValue

/**
 * The Pokemon on screen, with the cards either side of it showing through.
 *
 * Each card is drawn twice: the artwork, and the same artwork flattened to [silhouette] on top of
 * it. The flat copy's alpha is the card's distance from the centre, so a card arrives by resolving
 * out of the ground colour and leaves by dissolving back into it. That is cheaper and steadier than
 * animating a colour filter, which would rebuild the filter every frame.
 *
 * The pages are a fixed width rather than the viewport's, because what a neighbour shows has to be a
 * slice of the *artwork* and not a slice of a page with the artwork somewhere inside it.
 *
 * DECISIONS.md § The carousel is a pager over the browse list
 */
@Composable
fun HeroCarousel(
    heroes: List<DetailHeroUiModel>,
    silhouette: Color,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        // Centres a fixed-width page in whatever width the screen turns out to be.
        val sidePadding = ((maxWidth - HERO_SIZE) / 2).coerceAtLeast(0.dp)

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(HERO_SIZE),
            contentPadding = PaddingValues(horizontal = sidePadding),
            pageSpacing = HERO_SPACING,
        ) { page ->
            val hero = heroes[page]

            // 0 at the centre, 1 a full page away. Read inside graphicsLayer where it can be, so a
            // drag redraws rather than recomposing a thousand-page pager.
            val distanceOf = {
                ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    .absoluteValue
                    .coerceIn(0f, 1f)
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = lerp(NEIGHBOUR_SCALE, 1f, 1f - distanceOf())
                            scaleX = scale
                            scaleY = scale
                        },
            ) {
                AsyncImage(
                    model = hero.artworkUrl,
                    // The name is read out above it, so describing the artwork too would say every
                    // Pokemon twice to a screen reader.
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().sharedElementOrNone(hero.artworkKey),
                )

                AsyncImage(
                    model = hero.artworkUrl,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(silhouette),
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = distanceOf() },
                )
            }
        }
    }
}

private val HERO_SIZE = 200.dp

// Wide enough that a neighbour shows about a fifth of itself once it has been scaled down. Any less
// and the cards look like one picture with edges; any more and the centre stops being the subject.
private val HERO_SPACING = 56.dp

// Smaller, not distant: the neighbours are the same objects seen past the one in front.
private const val NEIGHBOUR_SCALE = 0.75f
