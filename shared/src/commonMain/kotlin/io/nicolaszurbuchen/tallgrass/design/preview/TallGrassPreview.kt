package io.nicolaszurbuchen.tallgrass.design.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.design.theme.TallGrassTheme
import io.nicolaszurbuchen.tallgrass.design.theme.appColors

/**
 * Wraps preview content in the app theme and fills the background behind it.
 *
 * Both halves are load-bearing: the preview pane paints its own white regardless of the theme, so
 * content that does not fill its background renders wrong in dark mode and still looks plausible.
 * See DECISIONS.md § A preview brings its own ground.
 */
@Composable
fun TallGrassPreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TallGrassTheme {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.background)) {
            content()
        }
    }
}
