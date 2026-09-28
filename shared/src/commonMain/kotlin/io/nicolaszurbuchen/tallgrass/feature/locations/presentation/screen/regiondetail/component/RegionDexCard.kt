package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.core.type.presentation.component.TypePill
import io.nicolaszurbuchen.tallgrass.design.component.AppPokeball
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionDexCardUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One card in a region's Pokedex tab.
 *
 * **A second dex card rather than the dex screen's own.** #24 said this tab reuses the main grid and
 * has nothing new to build, and that is right about the data -- but `DexCard` is bound to the Pokedex
 * feature's shared-element keys and its `DexEntryUiModel`, and a feature may not import from another
 * feature. Lifting it into `core/pokemon/presentation/` is the move that would make #24 literally
 * true, and it touches the signature transition, so it is not something to do on the way past.
 *
 * The consequence is that opening a Pokemon from here is a push rather than the shared element #11's
 * rule would classify it as. Recorded in `DECISIONS.md`; the transition arrives with the card.
 */
@Composable
fun RegionDexCard(
    entry: RegionDexCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = entry.primaryType.color, contentColor = Color.White),
        onClick = onClick,
        modifier = modifier.fillMaxWidth().aspectRatio(CARD_RATIO),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AppPokeball(
                color = Color.White.copy(alpha = ORNAMENT_ALPHA),
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .requiredSize(ORNAMENT_SIZE)
                        .offset(x = ORNAMENT_OFFSET, y = ORNAMENT_OFFSET),
            )

            AsyncImage(
                model = entry.artworkUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(ART_SIZE)
                        .offset(x = ART_OFFSET, y = ART_OFFSET),
            )

            Column(modifier = Modifier.padding(MaterialTheme.spacing.md)) {
                Text(
                    text = entry.numberText.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = NUMBER_ALPHA),
                )

                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                        TypePill(type = entry.primaryType)
                    }
                    entry.secondaryType?.let { TypePill(type = it) }
                }
            }
        }
    }
}

private const val CARD_RATIO = 1.2f

// Behind the artwork and barely there, the same ornament the dex grid draws.
private const val ORNAMENT_ALPHA = 0.16f
private const val NUMBER_ALPHA = 0.75f

private val ORNAMENT_SIZE = 96.dp
private val ORNAMENT_OFFSET = 18.dp
private val ART_SIZE = 74.dp
private val ART_OFFSET = 6.dp
