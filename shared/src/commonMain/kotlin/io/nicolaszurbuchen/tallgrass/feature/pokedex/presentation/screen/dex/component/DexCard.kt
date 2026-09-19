package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.ARTWORK_OVERLAY_Z
import io.nicolaszurbuchen.tallgrass.infra.navigation.LocalSharedTransitionScope
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey

/**
 * One Pokemon in the dex grid.
 *
 * [artworkKey] and [tintKey] are non-null only for the card that is travelling into the detail,
 * which is at most one card on screen. Null keys draw exactly the same card and register nothing.
 * See `DECISIONS.md § Only the tapped card is a shared element`.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DexCard(
    name: String,
    numberText: String,
    formLabel: String?,
    artworkUrl: String,
    artworkKey: SharedElementKey?,
    tintKey: SharedElementKey?,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalNavAnimatedContentScope.current

    Box(modifier = modifier.aspectRatio(CARD_ASPECT_RATIO)) {
        // The card's colour, drawn behind the card that is already painted in it and so invisible
        // until it moves. On the way to the detail this is what lifts off the grid and grows into
        // the ground behind the hero, while the card it came from fades out with the rest of the
        // screen. sharedBounds rather than sharedElement: the two ends are different shapes, and
        // this is the one that cross-fades between them instead of popping.
        if (tintKey != null) {
            with(sharedTransitionScope) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(tintKey),
                                animatedVisibilityScope = animatedContentScope,
                                resizeMode = RemeasureToBounds,
                                // The colour is the same at both ends, so there is nothing to cross-fade between;
                                // the screen it belongs to is already dissolving and would fade it twice.
                                enter = EnterTransition.None,
                                exit = ExitTransition.None,
                                // Drawn where it lives rather than lifted into the shared-element overlay. Lifted,
                                // it covers the header, the sheet and every word on the screen for the whole
                                // transition -- the whole point is that it arrives *behind* them.
                                renderInOverlayDuringTransition = true,
                            )
                            .clip(MaterialTheme.shapes.medium)
                            .background(tint),
                )
            }
        }

        Card(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = tint, contentColor = Color.White),
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.sm),
            ) {
                Text(
                    text = numberText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = NUMBER_ALPHA),
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // The sending half of the transition into the detail hero. The key is the card's
                    // own, built once in the navigation package so both ends agree. See #11.
                    val artworkModifier =
                        if (artworkKey == null) {
                            Modifier.size(ARTWORK_SIZE)
                        } else {
                            with(sharedTransitionScope) {
                                Modifier
                                    .size(ARTWORK_SIZE)
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(artworkKey),
                                        animatedVisibilityScope = animatedContentScope,
                                        // Above the colour, which is travelling at the same time and
                                        // would otherwise cover the Pokemon for the whole flight.
                                        zIndexInOverlay = ARTWORK_OVERLAY_Z,
                                    )
                            }
                        }

                    AsyncImage(
                        model = artworkUrl,
                        // The name is read out immediately below, so describing the artwork too
                        // would have a screen reader say every Pokemon twice.
                        contentDescription = null,
                        modifier = artworkModifier,
                    )
                }

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (formLabel != null) {
                        Text(
                            text = formLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = NUMBER_ALPHA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// Slightly taller than square, so a two-line card -- one with a form chip -- does not have to grow
// and leave the row it sits in ragged.
private const val CARD_ASPECT_RATIO = 0.82f

private val ARTWORK_SIZE = 84.dp

// The number and the form label are supporting text on a saturated ground, where full white reads
// as loud as the name it sits under.
private const val NUMBER_ALPHA = 0.7f
