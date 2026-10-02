package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * A subject's own colour, pushed far enough off the sheet to be read as text.
 *
 * The eighteen type colours are chosen to be told apart from each other, not to be legible: Electric
 * measures 1.5:1 against a white sheet, Ice 1.6:1 and Steel 2.0:1, which is not a contrast ratio so
 * much as a suggestion. Moving each one toward the far end of the theme buys that back while keeping
 * the hue, so ice stays teal and grass stays green and the colour still says which type it is.
 *
 * For text only. A filled shape in a type's colour — a stat bar, a move card, a chip's ground —
 * wants the colour itself, because there is nothing to read *through* it.
 */
@Composable
@ReadOnlyComposable
fun Color.asLabelColor(): Color = lerp(this, if (MaterialTheme.appColors.isDark) Color.White else Color.Black, LABEL_SHIFT)

/**
 * Black or white, whichever can be read *on* this colour.
 *
 * [asLabelColor]'s opposite number. That one is for a subject's colour drawn as text on the sheet;
 * this is for text drawn on a shape already filled with it, where there is nothing to shift because
 * the ground is the colour.
 *
 * Neither answer works for all eighteen types, which is why this is a test rather than a constant:
 * Electric and Ice are nearly white and Dragon and Dark nearly black, so white-on-everything leaves
 * four chips at about 1.5:1 -- the same problem [asLabelColor] exists to solve, reached from the
 * other side.
 *
 * No theme lookup, unlike [asLabelColor]. The ground is the type's colour in both themes, so what
 * reads on it does not change with the sheet behind it.
 */
fun Color.asOnColor(): Color = if (luminance() > ON_COLOR_PIVOT) Color.Black else Color.White

// Where black and white are equally legible against a colour. It is the midpoint of the WCAG
// contrast formula rather than of the brightness range, which is why it is 0.179 and not 0.5.
private const val ON_COLOR_PIVOT = 0.179f

// How far a colour moves before it is worth reading. Enough that Electric, the palette's brightest,
// clears 4.5:1 on a white sheet; little enough that the hue is still recognisably the type's. The
// figure came from the matchup chips, where it was measured against a ground already washed with the
// same colour, so it is if anything generous here.
private const val LABEL_SHIFT = 0.45f
