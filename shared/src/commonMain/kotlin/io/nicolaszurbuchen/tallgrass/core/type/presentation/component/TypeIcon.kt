package io.nicolaszurbuchen.tallgrass.core.type.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.ic_type_bug
import tallgrass.shared.generated.resources.ic_type_dark
import tallgrass.shared.generated.resources.ic_type_dragon
import tallgrass.shared.generated.resources.ic_type_electric
import tallgrass.shared.generated.resources.ic_type_fairy
import tallgrass.shared.generated.resources.ic_type_fighting
import tallgrass.shared.generated.resources.ic_type_fire
import tallgrass.shared.generated.resources.ic_type_flying
import tallgrass.shared.generated.resources.ic_type_ghost
import tallgrass.shared.generated.resources.ic_type_grass
import tallgrass.shared.generated.resources.ic_type_ground
import tallgrass.shared.generated.resources.ic_type_ice
import tallgrass.shared.generated.resources.ic_type_normal
import tallgrass.shared.generated.resources.ic_type_poison
import tallgrass.shared.generated.resources.ic_type_psychic
import tallgrass.shared.generated.resources.ic_type_rock
import tallgrass.shared.generated.resources.ic_type_steel
import tallgrass.shared.generated.resources.ic_type_water

/**
 * One of the eighteen types as its symbol.
 *
 * In `core/type/` rather than `design/` for the reason [TypePill] is: it takes a [TypeUiModel], and
 * a component in `design/` may not know the subject exists.
 *
 * Decorative by default, because every place this is drawn names the type beside it. A caller that
 * draws it alone passes [contentDescription], and the label to pass is [TypeUiModel.label].
 *
 * The vectors are square and drawn in white, so the tint is the caller's: on a chip filled with the
 * type's own colour it is whatever reads on that, and the glyph is the one thing on the chip that
 * cannot be read by shape alone at this size.
 */
@Composable
fun TypeIcon(
    type: TypeUiModel,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(type.glyph()),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(color),
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Exhaustive by construction: a nineteenth type breaks this at compile time rather than at draw. */
private fun TypeUiModel.glyph(): DrawableResource =
    when (this) {
        TypeUiModel.NORMAL -> Res.drawable.ic_type_normal
        TypeUiModel.FIRE -> Res.drawable.ic_type_fire
        TypeUiModel.WATER -> Res.drawable.ic_type_water
        TypeUiModel.ELECTRIC -> Res.drawable.ic_type_electric
        TypeUiModel.GRASS -> Res.drawable.ic_type_grass
        TypeUiModel.ICE -> Res.drawable.ic_type_ice
        TypeUiModel.FIGHTING -> Res.drawable.ic_type_fighting
        TypeUiModel.POISON -> Res.drawable.ic_type_poison
        TypeUiModel.GROUND -> Res.drawable.ic_type_ground
        TypeUiModel.FLYING -> Res.drawable.ic_type_flying
        TypeUiModel.PSYCHIC -> Res.drawable.ic_type_psychic
        TypeUiModel.BUG -> Res.drawable.ic_type_bug
        TypeUiModel.ROCK -> Res.drawable.ic_type_rock
        TypeUiModel.GHOST -> Res.drawable.ic_type_ghost
        TypeUiModel.DRAGON -> Res.drawable.ic_type_dragon
        TypeUiModel.DARK -> Res.drawable.ic_type_dark
        TypeUiModel.STEEL -> Res.drawable.ic_type_steel
        TypeUiModel.FAIRY -> Res.drawable.ic_type_fairy
    }
