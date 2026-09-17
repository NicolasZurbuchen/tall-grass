package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypePill
import io.nicolaszurbuchen.tallgrass.design.theme.shimmerBlock
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.detail_back

/**
 * The text half of the hero, drawn on the type's colour.
 *
 * A null [content] is the moment before the read lands. The block keeps its height so the artwork
 * below it does not move when the name arrives, which is the whole reason the skeleton exists here
 * rather than a blank.
 */
@Composable
fun DetailHeader(
    content: DetailContentUiModel?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.md)) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.detail_back),
                tint = Color.White,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            if (content == null) {
                Box(modifier = Modifier.width(NAME_SKELETON_WIDTH).height(NAME_SKELETON_HEIGHT).shimmerBlock())
            } else {
                Text(
                    text = content.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = content.numberText,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.sm)
                    .heightIn(min = TYPES_ROW_MIN_HEIGHT),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                content?.types?.forEach { type -> TypePill(type = type) }
            }

            if (content != null) {
                Text(
                    text = content.genusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = GENUS_ALPHA),
                )
            }
        }
    }
}

private val NAME_SKELETON_WIDTH = 180.dp
private val NAME_SKELETON_HEIGHT = 32.dp

// Holds the row open while the types are unknown, so the artwork below does not jump when they land.
private val TYPES_ROW_MIN_HEIGHT = 24.dp

// Supporting text on a saturated ground, where full white reads as loud as the name above it.
private const val GENUS_ALPHA = 0.7f
