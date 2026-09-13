package io.nicolaszurbuchen.tallgrass.feature.home.presentation.screen.home

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes

@PreviewThemes
@Composable
private fun HomeScreenPreview() {
    TallGrassPreview {
        HomeScreen(onTileClick = { })
    }
}
