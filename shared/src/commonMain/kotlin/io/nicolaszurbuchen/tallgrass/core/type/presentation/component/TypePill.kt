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
 * A type, named and coloured.
 *
 * In `core/type/` rather than in `design/` because it owns a rule about the subject: which colour a
 * type is drawn in is a fact about the type, not a presentational choice the caller makes. The name
 * is the test — `AppTypePill` reads as nonsense.
 */
@Composable
fun TypePill(
    type: TypeUiModel,
    modifier: Modifier = Modifier,
) {
    Text(
        text = type.label,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White,
        modifier =
            modifier
                .clip(RoundedCornerShape(PILL_CORNER))
                .background(type.color)
                .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    )
}

// Larger than any radius in the shape scale, because a pill is a stadium rather than a rounded box.
private val PILL_CORNER = 999.dp
