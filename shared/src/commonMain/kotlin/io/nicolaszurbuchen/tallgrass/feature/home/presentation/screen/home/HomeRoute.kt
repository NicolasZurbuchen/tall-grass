package io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home.uimodel.HomeTileUiModel

/**
 * Home has no store: the eight tiles are a fixed list, not data, so there is nothing to load, fail
 * or reduce. If it ever gains state — a recently-viewed row, say — it grows the full
 * Contract/StoreFactory/ViewModel set rather than a bare `remember`.
 */
@Composable
fun HomeRoute(
    onTileClick: (HomeTileUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        onTileClick = onTileClick,
        modifier = modifier,
    )
}
