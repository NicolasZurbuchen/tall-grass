package io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.component.AppPokeball
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.component.HomeTile
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.uimodel.HomeTileUiModel
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.home_subtitle
import tallgrass.shared.generated.resources.home_title

@Composable
fun HomeScreen(
    onTileClick: (HomeTileUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        // Outside the system bars rather than inside them: the corner it is cropped by is the
        // screen's, so it runs up behind the status bar the way the tint on the detail screen does.
        AppPokeball(
            color = MaterialTheme.appColors.borderSubtle,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = CORNER_BALL_CROP_X, y = CORNER_BALL_CROP_Y)
                    .requiredSize(CORNER_BALL_SIZE),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(HOME_GRID_COLUMNS),
            contentPadding = PaddingValues(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(Res.string.home_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(Res.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
                )
            }

            items(HomeTileUiModel.entries) { tile ->
                HomeTile(
                    label = stringResource(tile.label),
                    color = tile.color,
                    onClick = { onTileClick(tile) },
                )
            }
        }
    }
}

// Two columns across eight tiles gives the 2x4 grid the home screen was designed around. A third
// column would fit on a tablet but is not worth a size class until there is a tablet layout.
private const val HOME_GRID_COLUMNS = 2

// Larger than a tile's ball by enough to read as a different object rather than as a ninth card,
// and hung far enough off both edges that only the band and part of the button are on screen.
private val CORNER_BALL_SIZE = 220.dp
private val CORNER_BALL_CROP_X = 72.dp
private val CORNER_BALL_CROP_Y = (-84).dp
