package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * The [SharedTransitionScope] opened by the navigation host.
 *
 * Fails rather than defaulting to null: reading it outside the host is a wiring mistake, and a null
 * default turns that into a transition that silently does not run.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope> {
        error("No SharedTransitionScope available. This must be read inside the navigation host.")
    }

/**
 * Registers this element as one half of a transition, or leaves the modifier untouched when [key] is
 * null.
 *
 * A null key is the ordinary case rather than a failure: at most one card in a grid is the one that
 * was tapped, and the rest draw exactly the same content while registering nothing. See
 * `DECISIONS.md § Only the tapped card is a shared element`.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElementOrNone(key: SharedElementKey?): Modifier {
    if (key == null) return this

    val transitionScope = LocalSharedTransitionScope.current
    val contentScope = LocalNavAnimatedContentScope.current
    val base = this

    return with(transitionScope) {
        base.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = contentScope,
        )
    }
}

/**
 * The same, for an element drawn at two different sizes.
 *
 * [sharedElementOrNone] animates the bounds and expects the content to fit whatever they become,
 * which is right for a picture and wrong for text: a name set at 13sp on a card and at 28sp in a
 * header is two different measurements, and re-laying it out every frame reflows the line under the
 * reader. This one scales the drawing instead, anchored left because both ends are left-aligned.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsOrNone(key: SharedElementKey?): Modifier {
    if (key == null) return this

    val transitionScope = LocalSharedTransitionScope.current
    val contentScope = LocalNavAnimatedContentScope.current
    val base = this

    return with(transitionScope) {
        base.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = contentScope,
            resizeMode =
                SharedTransitionScope.ResizeMode.scaleToBounds(
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.CenterStart,
                ),
        )
    }
}
