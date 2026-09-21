package io.nicolaszurbuchen.tallgrass.design.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.painterResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.ic_pokeball

/**
 * The app's watermark, in whatever colour and at whatever size the caller wants it.
 *
 * In `design/` rather than in `core/pokemon/` because it is the brand and not the subject: there is
 * no Pokeball in this app's domain, nothing reads one, and the shape says no more about the card it
 * is on than a logo says about the letterhead. `design/` is the layer that may know the brand --
 * what it may not know is the domain, and this does not.
 *
 * It draws only itself. Every call site crops it against an edge, and each one does that by sizing
 * and offsetting this past the bounds of something that clips -- a card, the screen. Nothing here
 * decides where the crop falls, because a watermark cropped by its own rules would need one rule per
 * corner it is ever put in.
 *
 * Decorative throughout, so it is never announced: a screen reader that read this would say
 * "pokeball" eight times on the home grid and once more behind every Pokemon.
 */
@Composable
fun AppPokeball(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(Res.drawable.ic_pokeball),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier,
    )
}
