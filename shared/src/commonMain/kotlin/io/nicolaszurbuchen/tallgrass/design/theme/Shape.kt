package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(48.dp),
    )

/**
 * A sheet whose top edge is an arc, highest in the middle and falling away to both sides.
 *
 * [rise] is how far the middle sits above the corners. The apex is the shape's own top, so a caller
 * that positions the sheet by its box gets the same overlap in the middle as a flat edge would —
 * what changes is that the ground shows through further down at the sides.
 *
 * A quadratic curve rather than a true circular arc: across a phone's width the two are within a
 * pixel of each other, and one control point is cheaper than solving for a radius that would have to
 * be recomputed whenever the width changes.
 */
fun arcTopShape(rise: Dp): Shape =
    object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Outline {
            // The control point sits as far above the apex as the corners sit below it, which is
            // what puts a quadratic's midpoint exactly on the shape's top edge.
            val riseP = with(density) { rise.toPx() }

            val path =
                Path().apply {
                    moveTo(0f, riseP)
                    quadraticTo(size.width / 2f, -riseP, size.width, riseP)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

            return Outline.Generic(path)
        }
    }
