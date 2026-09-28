package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionLocationUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * One place in a region's Locations list: a coloured marker, a name, and how many games have
 * anything to meet there.
 *
 * **The marker is the only reason this list can be scanned.** Sinnoh has 128 rows and a reader
 * looking for a cave is looking for brown rather than reading names. The colour is read out of the
 * slug at generation time and is decoration -- about one place in six is neutral, which is an
 * ordinary outcome rather than a hole.
 *
 * The row does not open anything yet. The location detail is the next screen to land, and a tap
 * target that does nothing is worse than one that is plainly not there -- so the row is a listing
 * until there is somewhere for it to go.
 */
@Composable
fun RegionLocationRow(
    location: RegionLocationUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surfaceRaised),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(MARKER_SIZE)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(location.category.color),
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = location.gamesText.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    // A place with nothing in it says so in the quieter of the two colours, so the
                    // list reads as mostly-full rather than as mostly-broken.
                    color =
                        if (location.hasEncounters) {
                            MaterialTheme.appColors.textSecondary
                        } else {
                            MaterialTheme.appColors.textTertiary
                        },
                    maxLines = 1,
                )
            }
        }
    }
}

// A square rather than an icon: nobody has drawn eleven kinds of place, and a shape that is plainly
// a colour swatch does not pretend to be a picture of a cave.
private val MARKER_SIZE = 12.dp
