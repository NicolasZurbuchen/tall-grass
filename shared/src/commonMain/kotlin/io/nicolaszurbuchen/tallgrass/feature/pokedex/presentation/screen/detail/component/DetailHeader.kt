package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
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
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_back

/**
 * The text half of the hero, drawn on the type's colour.
 *
 * [name], [numberText] and [types] come from the card the carousel is on, so they are drawn on the
 * first frame and change the instant a swipe crosses. The name and the types are also the receiving
 * half of the transition from a dex card, so they take no entrance of their own — they are already
 * moving. A null [content] is the moment before the read lands, which leaves only the genus to
 * appear.
 */
@Composable
fun DetailHeader(
    name: String,
    types: List<TypeUiModel>,
    numberText: String,
    artworkKey: SharedElementKey?,
    content: DetailContentUiModel?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    elapsedMillis: Int = ENTRANCE_DONE,
) {
    // The two texts below trail their rows, so which way "in from the end" points is the layout's
    // to answer rather than the modifier's.
    val layoutDirection = LocalLayoutDirection.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Deliberately outside the entrance. It is the one control on this screen that has to work
        // the instant the screen is up, and a target that is still sliding is a target that can be
        // missed. The inset is Material's navigation-icon padding rather than this screen's gutter.
        // DECISIONS.md § A back arrow takes the navigation inset, not the content gutter
        IconButton(onClick = onBackClick, modifier = Modifier.padding(start = MaterialTheme.spacing.xs)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.pokedex_detail_back),
                tint = Color.White,
            )
        }

        // Baselines rather than tops: the number is a fifth of the name's size, and aligning their
        // boxes left it floating somewhere above the name's midline.
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.lg)
                    .padding(top = MaterialTheme.spacing.md),
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
                    .padding(horizontal = MaterialTheme.spacing.lg)
                    .padding(top = MaterialTheme.spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                // Bounds rather than a plain shared element: the pill is a chip on the card and a
                // headline element here, so the two halves are different sizes.
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

// Supporting text on a saturated ground, where full white reads as loud as the name above it.
private const val GENUS_ALPHA = 0.7f
