package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.move.presentation.component.DamageClassIcon
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypePill
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.header_back

/**
 * The move's own colour, its name, and what it is.
 *
 * The card the reader tapped grown to the width of the screen: same ground, same class glyph in the
 * corner, same pill. It does not collapse into a toolbar the way a Pokemon's hero does -- there is
 * nothing to drag it out of the way of.
 */
@Composable
fun MoveDetailHeader(
    move: MoveContentUiModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(HERO_SHAPE)
                .background(move.type.color),
    ) {
        // **What a Pokemon's hero puts its artwork in.** A move has no picture, and an empty coloured
        // block is what made this screen feel like it was missing something; the class glyph is the
        // one thing a move has that is worth drawing large. In a matchParentSize layer so it cannot
        // set the hero's height, for the same reason a move card's is.
        Box(modifier = Modifier.matchParentSize()) {
            DamageClassIcon(
                damageClass = move.damageClass,
                color = Color.White.copy(alpha = GLYPH_ALPHA),
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = GLYPH_CROP)
                        .requiredWidth(GLYPH_WIDTH),
            )
        }

        // The inset is on the content and not on the box, which is what lets the colour run behind
        // the status bar while the back arrow still clears the clock.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = MaterialTheme.spacing.lg),
        ) {
            // Outside the content gutter: the arrow takes Material's navigation inset and the text
            // does not. DECISIONS.md § A back arrow takes the navigation inset, not the content gutter
            Box(modifier = Modifier.fillMaxWidth().height(TOOLBAR_ROW_HEIGHT)) {
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                modifier =
                    Modifier
                        .padding(horizontal = MaterialTheme.spacing.md)
                        .padding(top = MaterialTheme.spacing.md),
            ) {
                TypePill(type = move.type, style = MaterialTheme.typography.titleMedium)

                // The one figure that is a property of the move rather than of what it does, so it
                // sits with the identity rather than in the bars below.
                move.ppText?.let { pp ->
                    Text(
                        text = pp.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = PP_ALPHA),
                    )
                }
            }
        }
    }
}

// Square at the top, where the status bar sits over it, and curved into the body below.
private val HERO_SHAPE = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

// The same height Material gives a toolbar row, so the arrow lands where a reader expects it even
// though there is no toolbar here.
private val TOOLBAR_ROW_HEIGHT = 56.dp

// Taller than the hero, so the glyph runs off both the top and the bottom of it and reads as
// something the screen is a window onto rather than as a picture placed in it.
private val GLYPH_WIDTH = 320.dp

private val GLYPH_CROP = GLYPH_WIDTH * 0.2f

// Fainter than a card's. It is spread over much more of the screen here, and the name sits on it.
private const val GLYPH_ALPHA = 0.16f

// Supporting text beside a pill and a glyph that are both full white.
private const val PP_ALPHA = 0.8f
