package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DexUiMapperTest {
    private fun entry(
        slug: String = "bulbasaur",
        dexNumber: Int = 1,
        name: String = "Bulbasaur",
        formLabel: String? = null,
        primaryType: PokemonType = PokemonType.GRASS,
    ) = DexEntry(
        slug = slug,
        dexNumber = dexNumber,
        name = name,
        formLabel = formLabel,
        artworkUrl = "https://example.invalid/1.png",
        primaryType = primaryType,
        secondaryType = null,
    )

    @Test
    fun toUiModel_padsTheDexNumberToThreeDigits() {
        val ui = DexState(entries = listOf(entry(dexNumber = 1))).toUiModel()

        assertEquals("#001", ui.entries.single().numberText)
    }

    @Test
    fun toUiModel_doesNotTruncateAFourDigitNumber() {
        // Padding is a minimum, not a width. The National Dex is close enough to 1000 that a mapper
        // which formatted to exactly three digits would start losing a digit within a generation.
        val ui = DexState(entries = listOf(entry(dexNumber = 1025))).toUiModel()

        assertEquals("#1025", ui.entries.single().numberText)
    }

    @Test
    fun toUiModel_tintsByThePrimaryType() {
        val ui = DexState(entries = listOf(entry(primaryType = PokemonType.ICE))).toUiModel()

        assertEquals(TypeUiModel.ICE.color, ui.entries.single().tint)
    }

    @Test
    fun toUiModel_shortensTheFormLabelForTheChip() {
        // The dataset stores upstream's "Alolan Form"; a chip that size reads better as "Alolan".
        val ui = DexState(entries = listOf(entry(formLabel = "Alolan Form"))).toUiModel()

        assertEquals("Alolan", ui.entries.single().formLabel)
    }

    @Test
    fun toUiModel_leavesAFormLabelWithoutTheSuffixAlone() {
        val ui = DexState(entries = listOf(entry(formLabel = "Mega Charizard X"))).toUiModel()

        assertEquals("Mega Charizard X", ui.entries.single().formLabel)
    }

    @Test
    fun toUiModel_hasNoChipForAnOrdinaryForm() {
        assertNull(DexState(entries = listOf(entry())).toUiModel().entries.single().formLabel)
    }

    @Test
    fun toUiModel_givesEachCardTheKeyItsArtworkFliesUnder() {
        // Both halves of the transition build the key through the same function, so a card and the
        // hero it opens agree by construction rather than by two literals staying in step.
        val ui = DexState(entries = listOf(entry(slug = "vulpix-alola"))).toUiModel()

        assertEquals(dexArtworkKey("vulpix-alola"), ui.entries.single().artworkKey)
    }

    @Test
    fun toUiModel_carriesLoadingAndErrorThrough() {
        assertEquals(true, DexState(isLoading = true).toUiModel().isLoading)
        assertNotNull(DexState(error = AppError.Unexpected(IllegalStateException())).toUiModel().error)
    }
}
