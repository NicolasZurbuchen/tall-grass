package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypePill
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation.typeKey
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedBoundsOrNone
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.header_back

/**
 * The move's name and type, and the toolbar it turns into.
 *
 * [collapseProgress] is how far the sheet below has been dragged up. Everything here goes with it
 * except the back arrow, which never moves, and the name, which travels to the middle of the arrow's
 * row and shrinks to a title. The same behaviour a Pokemon's hero has, for the same reason — see
 * `DECISIONS.md § The sheet expands and the hero becomes a toolbar`.
 *
 * There is no figure beside the name. PP was here and moved into the sheet with the other two
 * numbers: it is a number to compare, and the three of them belong together rather than one of them
 * standing in for the set.
 */
@Composable
fun MoveDetailHeader(
    move: MoveContentUiModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapseProgress: Float = 0f,
) {
    val density = LocalDensity.current

    // Front-loaded on purpose. The sheet is what the reader is moving, so everything it is taking
    // the place of should be gone by the time they have decided to move it.
    val heroAlpha = 1f - (collapseProgress / HERO_FADE_BY).coerceIn(0f, 1f)

    // The name is one Text that moves rather than two that cross-fade, so its width has to be known
    // before its destination can be: a title is centred against its own measurement.
    var nameWidth by remember { mutableStateOf(0.dp) }
    val titleWidth = nameWidth * TOOLBAR_NAME_SCALE

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gutter = MaterialTheme.spacing.lg
        val travelX = (maxWidth - titleWidth) / 2 - gutter

        Column(modifier = Modifier.fillMaxWidth()) {
            // Outside the gutter: the arrow takes Material's navigation inset and the text does not.
            // DECISIONS.md § A back arrow takes the navigation inset, not the content gutter
            Box(modifier = Modifier.fillMaxWidth().height(TOOLBAR_ROW_HEIGHT)) {
                // Deliberately outside the fade. It is the one control on this screen that has to
                // work whatever else is happening, and a target that is still moving can be missed.
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = MaterialTheme.spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.header_back),
                        tint = Color.White,
                    )
                }
            }

            Text(
                text = move.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .padding(horizontal = gutter)
                        .padding(top = MaterialTheme.spacing.md)
                        .onSizeChanged { nameWidth = with(density) { it.width.toDp() } }
                        .sharedBoundsOrNone(move.nameKey)
                        .graphicsLayer {
                            // From its own left edge, so the travel is a translation of a known width
                            // rather than a guess about where the middle of a line is.
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            val scale = lerp(1f, TOOLBAR_NAME_SCALE, collapseProgress)
                            scaleX = scale
                            scaleY = scale
                            translationY = -collapseProgress * NAME_RISE.toPx()
                            translationX = collapseProgress * travelX.toPx()
                        },
            )

            TypePill(
                type = move.type,
                modifier =
                    Modifier
                        .padding(horizontal = gutter)
                        .padding(top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.lg)
                        .graphicsLayer { alpha = heroAlpha }
                        .sharedBoundsOrNone(move.nameKey.typeKey()),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = PILL_SIZE),
            )
        }
    }
}

// The arrow's row, which is what the header is left as when the sheet is all the way up.
private val TOOLBAR_ROW_HEIGHT = 56.dp

// Where the name ends up: the middle of that row, at about the size of a toolbar title. The rise is
// the row's own height plus the gap between it and the name's line.
private const val TOOLBAR_NAME_SCALE = 0.45f
private val NAME_RISE = 64.dp

// Larger than a Pokemon hero's 14sp, and larger than the slot it is copied from. This hero has one
// pill where a Pokemon's has two plus a number and a genus line, so at chip size it read as a label
// somebody had forgotten to finish rather than as the move's second fact.
private val PILL_SIZE = 18.sp

// The hero is gone in the first quarter of the drag.
private const val HERO_FADE_BY = 0.25f
