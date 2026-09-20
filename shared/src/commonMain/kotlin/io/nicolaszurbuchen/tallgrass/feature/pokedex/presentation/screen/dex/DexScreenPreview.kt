package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.design.preview.TallGrassPreview
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.uimodel.PrefetchUiModel
import io.nicolaszurbuchen.tallgrass.infra.preview.PreviewThemes
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

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
                            DexEntryUiModel(
                                slug = "bulbasaur",
                                numberText = "#001",
                                name = "Bulbasaur",
                                types = listOf(TypeUiModel.GRASS, TypeUiModel.POISON),
                                artworkUrl = "",
                                artworkKey = dexArtworkKey("bulbasaur"),
                                tint = TypeUiModel.GRASS.color,
                            ),
                            DexEntryUiModel(
                                slug = "charmander",
                                numberText = "#004",
                                name = "Charmander",
                                types = listOf(TypeUiModel.FIRE),
                                artworkUrl = "",
                                artworkKey = dexArtworkKey("charmander"),
                                tint = TypeUiModel.FIRE.color,
                            ),
                            DexEntryUiModel(
                                slug = "squirtle",
                                numberText = "#007",
                                name = "Squirtle",
                                types = listOf(TypeUiModel.WATER),
                                artworkUrl = "",
                                artworkKey = dexArtworkKey("squirtle"),
                                tint = TypeUiModel.WATER.color,
                            ),
                            DexEntryUiModel(
                                slug = "vulpix",
                                numberText = "#037",
                                name = "Vulpix",
                                types = listOf(TypeUiModel.FIRE),
                                artworkUrl = "",
                                artworkKey = dexArtworkKey("vulpix"),
                                tint = TypeUiModel.FIRE.color,
                            ),
                            DexEntryUiModel(
                                slug = "vulpix-alola",
                                numberText = "#037",
                                name = "Alolan Vulpix",
                                types = listOf(TypeUiModel.ICE),
                                artworkUrl = "",
                                artworkKey = dexArtworkKey("vulpix-alola"),
                                tint = TypeUiModel.ICE.color,
                            ),
                            DexEntryUiModel(
                                slug = "pikachu",
                                numberText = "#025",
                                name = "Pikachu",
                                types = listOf(TypeUiModel.ELECTRIC),
                                artworkUrl = "",
                                artworkKey = dexArtworkKey("pikachu"),
                                tint = TypeUiModel.ELECTRIC.color,
                            ),
                        ),
                    error = null,
                    prefetch =
                        PrefetchUiModel(
                            message = UiText.Raw("Saving artwork for offline — 42%"),
                            fraction = 0.42f,
                        ),
                ),
            onEntryClick = { },
            onBackClick = { },
            onRetryClick = { },
        )
    }
}
