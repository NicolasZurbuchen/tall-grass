package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.nicolaszurbuchen.tallgrass.infra.navigation.LocalSharedTransitionScope
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

/**
 * The ground behind the hero, and the receiving half of the colour's journey from the dex card.
 *
 * The card the reader tapped was a rectangle of exactly this colour, so rather than the detail
 * simply being this colour when it arrives, the card's own colour grows into it. It travels under
 * the artwork — see `ARTWORK_OVERLAY_Z` — and everything else on the screen cross-dissolves over
 * the top of it.
 *
 * [tintKey] matches a card only while the form on screen is the one that was tapped; after a switch
 * it deliberately matches nothing and the colour simply changes in place, as it did before.
 *
 * It fills the whole screen rather than only the visible band above the sheet. The sheet is drawn
 * over it and there is no screen height to compute, which is the same reason the artwork is laid out
 * where it is.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailTint(
    tint: Color,
    tintKey: SharedElementKey,
    modifier: Modifier = Modifier,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalNavAnimatedContentScope.current

    with(sharedTransitionScope) {
        Box(
            modifier =
                modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(tintKey),
                        animatedVisibilityScope = animatedContentScope,
                        // A colour has no layout to scale, and remeasuring is what keeps the corners
                        // it arrives with from being stretched on the way.
                        resizeMode = RemeasureToBounds,
                        // The colour is the same at both ends, so there is nothing to cross-fade between;
                        // the screen it belongs to is already dissolving and would fade it twice.
                        enter = EnterTransition.None,
                        exit = ExitTransition.None,
                        // Drawn where it lives rather than lifted into the shared-element overlay. Lifted,
                        // it covers the header, the sheet and every word on the screen for the whole
                        // transition -- the whole point is that it arrives *behind* them.
                        renderInOverlayDuringTransition = true,
                    )
                    .background(tint),
        )
    }
}
