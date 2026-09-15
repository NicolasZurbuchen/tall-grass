package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes

@PreviewThemes
@Composable
private fun DexScreenPreview() {
    TallGrassPreview {
        DexScreen(
            state =
                DexUiModel(
                    isLoading = false,
                    entries =
                        listOf(
                            DexEntryUiModel("bulbasaur", "#001", "Bulbasaur", null, "", TypeUiModel.GRASS.color),
                            DexEntryUiModel("charmander", "#004", "Charmander", null, "", TypeUiModel.FIRE.color),
                            DexEntryUiModel("squirtle", "#007", "Squirtle", null, "", TypeUiModel.WATER.color),
                            DexEntryUiModel("vulpix", "#037", "Vulpix", null, "", TypeUiModel.FIRE.color),
                            DexEntryUiModel("vulpix-alola", "#037", "Alolan Vulpix", "Alolan", "", TypeUiModel.ICE.color),
                            DexEntryUiModel("pikachu", "#025", "Pikachu", null, "", TypeUiModel.ELECTRIC.color),
                        ),
                    error = null,
                ),
            onEntryClick = { },
            onRetryClick = { },
        )
    }
}
