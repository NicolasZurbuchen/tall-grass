package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import kotlin.test.Test
import kotlin.test.assertEquals

class DexEntryUiMapperTest {
    private fun entry(
        slug: String = "bulbasaur",
        dexNumber: Int = 1,
        name: String = "Bulbasaur",
        primaryType: PokemonType = PokemonType.GRASS,
        secondaryType: PokemonType? = PokemonType.POISON,
    ) = DexEntry(
        slug = slug,
        dexNumber = dexNumber,
        name = name,
        formLabel = null,
        artworkUrl = "https://example.invalid/1.png",
        primaryType = primaryType,
        secondaryType = secondaryType,
    )

    @Test
    fun toUiModel_padsTheDexNumberToThreeDigits() {
        assertEquals("#001", entry(dexNumber = 1).toUiModel().numberText)
    }

    @Test
    fun toUiModel_doesNotTruncateAFourDigitNumber() {
        // Padding is a minimum, not a width. The National Dex is close enough to 1000 that a mapper
        // which formatted to exactly three digits would start losing a digit within a generation.
        assertEquals("#1025", entry(dexNumber = 1025).toUiModel().numberText)
    }

    @Test
    fun toUiModel_tintsByThePrimaryType() {
        assertEquals(TypeUiModel.ICE.color, entry(primaryType = PokemonType.ICE).toUiModel().tint)
    }

    @Test
    fun toUiModel_keepsBothTypesInSlotOrder() {
        assertEquals(listOf(TypeUiModel.GRASS, TypeUiModel.POISON), entry().toUiModel().types)
    }

    @Test
    fun toUiModel_givesASingleTypedPokemonOnePill() {
        assertEquals(listOf(TypeUiModel.FIRE), entry(primaryType = PokemonType.FIRE, secondaryType = null).toUiModel().types)
    }

    @Test
    fun toUiModel_givesEachCardTheKeyItsArtworkFliesUnder() {
        // Both halves of the transition build the key through the same function, so a card and the
        // hero it opens agree by construction rather than by two literals staying in step.
        assertEquals(dexArtworkKey("vulpix-alola"), entry(slug = "vulpix-alola").toUiModel().artworkKey)
    }
}
