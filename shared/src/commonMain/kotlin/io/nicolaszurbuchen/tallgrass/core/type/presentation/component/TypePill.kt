package io.nicolaszurbuchen.tallgrass.core.type.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel

/**
 * A type, named.
 *
 * In `core/type/` rather than in `design/` because it takes a [TypeUiModel], and a component in
 * `design/` may not know the subject exists. The name is the second test — `AppTypePill` reads as
 * nonsense.
 *
 * The fill is a scrim rather than the type's own colour. See
 * `DECISIONS.md § A type pill on the type's own colour is a scrim, not a colour`.
 *
 * [style] is the caller's because the same pill is a label on a grid card and a headline element on
 * a hero, and those are not the same size. It defaults to the chip slot, which is what a card wants.
 */
@Composable
fun TypePill(
    type: TypeUiModel,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall,
) {
    Text(
        text = type.label,
        style = style,
        color = Color.White,
        modifier =
            modifier
                .clip(RoundedCornerShape(PILL_CORNER))
                .background(Color.White.copy(alpha = SCRIM_ALPHA))
                .padding(horizontal = PILL_PADDING_HORIZONTAL, vertical = PILL_PADDING_VERTICAL),
    )
}

// Larger than any radius in the shape scale, because a pill is a stadium rather than a rounded box.
private val PILL_CORNER = 999.dp

// Two off the spacing scale in each direction, which is the one place in the app that leaves it. A
// stadium curves away from its label at both ends, so a pill padded to the scale has visibly less
// room around its text than a box padded by the same amount.
private val PILL_PADDING_HORIZONTAL = 10.dp
private val PILL_PADDING_VERTICAL = 6.dp

// Enough to separate the pill from the tint behind it without the label losing its white.
private const val SCRIM_ALPHA = 0.25f
