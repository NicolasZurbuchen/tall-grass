package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailHeroUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedElementOrNone
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_show_pokemon
import kotlin.math.absoluteValue

/**
 * The Pokemon on screen, with the cards either side of it standing behind it.
 *
 * Each card is drawn twice: the artwork, and the same artwork flattened to [silhouette] on top of
 * it. The flat copy's alpha is the card's distance from the centre, so a card arrives by resolving
 * out of the ground colour and leaves by dissolving back into it. That is cheaper and steadier than
 * animating a colour filter, which would rebuild the filter every frame.
 *
 * A neighbour is also a control: tapping it brings it to the centre, which is the same movement the
 * swipe makes and the only one available to a reader who cannot make the gesture.
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
    val scope = rememberCoroutineScope()

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

            // No ripple: the target is a Pokemon-shaped hole in a flat colour, and a circle
            // expanding out of it lands mostly on the background.
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = lerp(NEIGHBOUR_SCALE, 1f, 1f - distanceOf())
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = page != pagerState.currentPage,
                            onClickLabel = stringResource(Res.string.pokedex_detail_show_pokemon, hero.name),
                        ) {
                            scope.launch { pagerState.animateScrollToPage(page) }
                        },
            ) {
                AsyncImage(
                    model = hero.artworkUrl,
                    // The name is read out above it, so describing the artwork too would say every
                    // Pokemon twice to a screen reader. The tap target carries the name instead.
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

// All but touching. Halved, a neighbour is 100dp wide, and the page either side of the centre then
// has about 55 of those to show -- the half the design asks for.
private val HERO_SPACING = 4.dp

// Half. The size change is half of what makes a card arrive: it grows into the centre and shrinks
// out of it, so the movement is not only sideways.
private const val NEIGHBOUR_SCALE = 0.5f
