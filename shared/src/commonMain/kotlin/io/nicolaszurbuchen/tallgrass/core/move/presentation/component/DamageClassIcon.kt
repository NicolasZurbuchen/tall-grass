package io.nicolaszurbuchen.tallgrass.core.move.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.ic_move_physical
import tallgrass.shared.generated.resources.ic_move_special
import tallgrass.shared.generated.resources.ic_move_status

/**
 * Physical, Special or Status as a symbol.
 *
 * **It is the subject of a move card, not a label on one.** A Pokemon card has artwork and this is
 * what a move has instead, so it is drawn at the size artwork is drawn at and cropped by the same
 * corner. There are only three of them, which is what makes that work: a reader learns three shapes
 * once and then stops reading the card at all.
 *
 * In `core/move/` rather than `design/` for the same reason `TypePill` is in `core/type/` -- it takes
 * a [DamageClassUiModel], and a component in `design/` may not know the subject exists.
 *
 * Decorative by default, because the three cards it appears on all name the move beside it. A caller
 * that draws it where nothing else says which class this is passes [contentDescription]; the label
 * to pass is [DamageClassUiModel.label].
 *
 * **A caller sizes this by width alone and the height follows**, because the aspect is applied here
 * rather than left to the call site. The vectors are 3:2 and their intrinsic height is 16dp, so a
 * width without a matching height gets a 16dp-tall layout node with the glyph shrunk to fit it —
 * which at watermark sizes means an invisible mark rather than a wrong one.
 */
@Composable
fun DamageClassIcon(
    damageClass: DamageClassUiModel,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(damageClass.glyph()),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(color),
        contentScale = ContentScale.Fit,
        modifier = modifier.aspectRatio(GLYPH_ASPECT_RATIO),
    )
}

// The vectors' own, and the reason this is here and not at the call sites: the three share it, and a
// caller that had to know it could size one of them wrong without the shape looking wrong.
private const val GLYPH_ASPECT_RATIO = 1.5f

private fun DamageClassUiModel.glyph(): DrawableResource =
    when (this) {
        DamageClassUiModel.PHYSICAL -> Res.drawable.ic_move_physical
        DamageClassUiModel.SPECIAL -> Res.drawable.ic_move_special
        DamageClassUiModel.STATUS -> Res.drawable.ic_move_status
    }
