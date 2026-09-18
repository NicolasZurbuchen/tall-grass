package io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(HOME_GRID_COLUMNS),
        contentPadding = PaddingValues(MaterialTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        modifier = modifier.fillMaxSize().systemBarsPadding(),
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
                icon = tile.icon,
                onClick = { onTileClick(tile) },
            )
        }
    }
}

// Two columns across eight tiles gives the 2x4 grid the home screen was designed around. A third
// column would fit on a tablet but is not worth a size class until there is a tablet layout.
private const val HOME_GRID_COLUMNS = 2
