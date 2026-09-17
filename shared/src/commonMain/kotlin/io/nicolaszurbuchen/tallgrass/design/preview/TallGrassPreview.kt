package io.nicolaszurbuchen.tallgrass.design.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.nicolaszurbuchen.tallgrass.design.theme.TallGrassTheme
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.infra.navigation.LocalSharedTransitionScope

/**
 * Wraps preview content in the app theme, fills the background behind it, and stands in for the two
 * scopes the navigation host opens.
 *
 * The theme and the ground are load-bearing: the preview pane paints its own white regardless of the
 * theme, so content that does not fill its background renders wrong in dark mode and still looks
 * plausible. See DECISIONS.md § A preview brings its own ground.
 *
 * The scopes are load-bearing for a different reason. A shared element reads both of them and both
 * fail rather than defaulting, so any screen carrying one is unpreviewable without a host — and the
 * transition is the point of several of these screens. The `AnimatedContent` here never changes
 * state, so nothing animates; it exists only to open an `AnimatedContentScope`.
 */
@Composable
fun TallGrassPreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TallGrassTheme {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.background)) {
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    AnimatedContent(targetState = Unit) {
                        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                            content()
                        }
                    }
                }
            }
        }
    }
}
