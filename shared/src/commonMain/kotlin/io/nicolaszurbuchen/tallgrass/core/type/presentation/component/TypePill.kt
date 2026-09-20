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
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.theme.spacing

/**
 * A type, named.
 *
 * In `core/type/` rather than in `design/` because it takes a [TypeUiModel], and a component in
 * `design/` may not know the subject exists. The name is the second test — `AppTypePill` reads as
 * nonsense.
 *
 * The fill is a scrim rather than the type's own colour. See
 * `DECISIONS.md § A type pill on the type's own colour is a scrim, not a colour`.
 */
@Composable
fun TypePill(
    type: TypeUiModel,
    modifier: Modifier = Modifier,
) {
    Text(
        text = type.label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier =
            modifier
                .clip(RoundedCornerShape(PILL_CORNER))
                .background(Color.White.copy(alpha = SCRIM_ALPHA))
                .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    )
}

// Larger than any radius in the shape scale, because a pill is a stadium rather than a rounded box.
private val PILL_CORNER = 999.dp

// Enough to separate the pill from the tint behind it without the label losing its white.
private const val SCRIM_ALPHA = 0.25f
