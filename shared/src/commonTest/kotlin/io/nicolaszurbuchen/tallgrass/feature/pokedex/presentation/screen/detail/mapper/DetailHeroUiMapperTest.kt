package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailHeroUiMapperTest {
    private val charizard =
        DexEntry(
            slug = "charizard",
            dexNumber = 6,
            name = "Charizard",
            formLabel = null,
            artworkUrl = "https://example.invalid/6.png",
            primaryType = PokemonType.FIRE,
            secondaryType = PokemonType.FLYING,
        )

    @Test
    fun toHeroUiModel_drawsANeighbourAsItself() {
        val hero = charizard.toHeroUiModel()

        assertEquals("charizard", hero.slug)
        assertEquals(charizard.artworkUrl, hero.artworkUrl)
        assertEquals(TypeUiModel.FIRE.color, hero.tint)
    }

    @Test
    fun toHeroUiModel_registersNothingForANeighbour() {
        // Only the centred card can be carrying the key from the grid, and only while the form on
        // screen is still the one that was tapped.
        assertNull(charizard.toHeroUiModel().artworkKey)
    }

    @Test
    fun toHeroUiModel_takesTheCentredCardsArtworkAndKey() {
        // The switcher can move the centred card off its species' default picture, so what it draws
        // is the caller's to say rather than the entry's.
        val hero =
            charizard.toHeroUiModel(
                artworkUrl = "https://example.invalid/10034.png",
                tint = TypeUiModel.DRAGON.color,
                artworkKey = dexArtworkKey("charizard-mega-x"),
            )

        assertEquals("https://example.invalid/10034.png", hero.artworkUrl)
        assertEquals(TypeUiModel.DRAGON.color, hero.tint)
        assertEquals(dexArtworkKey("charizard-mega-x"), hero.artworkKey)
    }
}
