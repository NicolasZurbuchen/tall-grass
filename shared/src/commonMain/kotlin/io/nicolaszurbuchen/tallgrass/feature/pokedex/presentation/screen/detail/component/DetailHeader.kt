package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypePill
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.theme.ENTRANCE_DONE
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.slideInFromEnd
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.nameKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.typeKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedBoundsOrNone
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedElementOrNone
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_back
import tallgrass.shared.generated.resources.pokedex_detail_next
import tallgrass.shared.generated.resources.pokedex_detail_previous

/**
 * The text half of the hero, and the toolbar it turns into.
 *
 * [name], [numberText] and [types] come from the card the carousel is on, so they are drawn on the
 * first frame and change the instant a swipe crosses. The name and the types are also the receiving
 * half of the transition from a dex card, so they take no entrance of their own — they are already
 * moving. A null [content] is the moment before the read lands, which leaves only the genus to
 * appear.
 *
 * [collapseProgress] is how far the sheet below has been dragged up. Everything here goes with it
 * except the back arrow, which never moves, and the name, which travels to the middle of the arrow's
 * row and shrinks to a title with a chevron either side of it. See
 * `DECISIONS.md § The sheet expands and the hero becomes a toolbar`.
 */
@Composable
fun DetailHeader(
    name: String,
    types: List<TypeUiModel>,
    numberText: String,
    artworkKey: SharedElementKey?,
    content: DetailContentUiModel?,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    modifier: Modifier = Modifier,
    elapsedMillis: Int = ENTRANCE_DONE,
    collapseProgress: Float = 0f,
) {
    // The two texts below trail their rows, so which way "in from the end" points is the layout's
    // to answer rather than the modifier's.
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    // Front-loaded on purpose. The sheet is what the reader is moving, so everything it is taking
    // the place of should be gone by the time they have decided to move it.
    // DECISIONS.md § The sheet expands and the hero becomes a toolbar
    val heroAlpha = 1f - (collapseProgress / HERO_FADE_BY).coerceIn(0f, 1f)
    val toolbarAlpha = ((collapseProgress - TOOLBAR_FADE_FROM) / (1f - TOOLBAR_FADE_FROM)).coerceIn(0f, 1f)

    // The name is one Text that moves rather than two that cross-fade, so its width has to be known
    // before its destination can be: a title is centred against its own measurement.
    var nameWidth by remember { mutableStateOf(0.dp) }
    val titleWidth = nameWidth * TOOLBAR_NAME_SCALE

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gutter = MaterialTheme.spacing.lg
        val travelX = (maxWidth - titleWidth) / 2 - gutter

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = gutter)) {
            Box(modifier = Modifier.fillMaxWidth().height(TOOLBAR_ROW_HEIGHT)) {
                // Deliberately outside the entrance and outside the fade. It is the one control on
                // this screen that has to work whatever else is happening, and a target that is
                // still moving is a target that can be missed.
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = MaterialTheme.spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.pokedex_detail_back),
                        tint = Color.White,
                    )
                }

                // The chevrons flank a gap the size of the title, so the name lands between them
                // rather than beside them. They are the carousel's swipe as a pair of buttons.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Center).graphicsLayer { alpha = toolbarAlpha },
                ) {
                    StepButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        label = stringResource(Res.string.pokedex_detail_previous),
                        enabled = hasPrevious && toolbarAlpha > 0f,
                        onClick = onPreviousClick,
                    )
                    Spacer(modifier = Modifier.width(titleWidth))
                    StepButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        label = stringResource(Res.string.pokedex_detail_next),
                        enabled = hasNext && toolbarAlpha > 0f,
                        onClick = onNextClick,
                    )
                }
            }

            // Baselines rather than tops: the number is a fifth of the name's size, and aligning
            // their boxes left it floating somewhere above the name's midline.
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .weight(1f, fill = false)
                            .alignByBaseline()
                            .onSizeChanged { nameWidth = with(density) { it.width.toDp() } }
                            .graphicsLayer {
                                // From its own left edge, so the travel is a translation of a known
                                // width rather than a guess about where the middle of a line is.
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                val scale = lerp(1f, TOOLBAR_NAME_SCALE, collapseProgress)
                                scaleX = scale
                                scaleY = scale
                                translationY = -collapseProgress * NAME_RISE.toPx()
                                translationX = collapseProgress * travelX.toPx()
                            }
                            .sharedBoundsOrNone(artworkKey?.nameKey()),
                )

                if (numberText.isNotEmpty()) {
                    Text(
                        text = numberText,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier =
                            Modifier
                                .alignByBaseline()
                                .graphicsLayer { alpha = heroAlpha }
                                .slideInFromEnd(entranceFraction(0, elapsedMillis), layoutDirection),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.sm)
                        .graphicsLayer { alpha = heroAlpha },
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                    types.forEachIndexed { slot, type ->
                        TypePill(
                            type = type,
                            modifier = Modifier.sharedBoundsOrNone(artworkKey?.typeKey(slot)),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                if (content != null) {
                    Text(
                        text = content.genusText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = GENUS_ALPHA),
                        modifier = Modifier.slideInFromEnd(entranceFraction(1, elapsedMillis), layoutDirection),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = if (enabled) 1f else DISABLED_ALPHA),
        )
    }
}

// Supporting text on a saturated ground, where full white reads as loud as the name above it.
private const val GENUS_ALPHA = 0.7f

private const val DISABLED_ALPHA = 0.35f

// The arrow's row, which is what the header is left as when the sheet is all the way up.
private val TOOLBAR_ROW_HEIGHT = 56.dp

// Where the name ends up: the middle of that row, at about the size of a toolbar title. The rise is
// the row's own height plus the gap between it and the name's line.
private const val TOOLBAR_NAME_SCALE = 0.45f
private val NAME_RISE = 64.dp

// The hero is gone in the first quarter of the drag, and the chevrons arrive in the last two fifths.
// Neither shares the middle with the other, so nothing is half-faded on top of something else.
private const val HERO_FADE_BY = 0.25f
private const val TOOLBAR_FADE_FROM = 0.6f
