package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class DexUiMapperTest {
    private fun entry(slug: String = "bulbasaur") =
        DexEntry(
            slug = slug,
            dexNumber = 1,
            name = "Bulbasaur",
            formLabel = null,
            artworkUrl = "https://example.invalid/1.png",
            primaryType = PokemonType.GRASS,
            secondaryType = null,
        )

    @Test
    fun toUiModel_mapsTheEntriesItWasNotGivenAny() {
        val ui = DexState(entries = listOf(entry())).toUiModel()

        assertEquals("bulbasaur", ui.entries.single().slug)
    }

    @Test
    fun toUiModel_usesTheCardsItWasHandedRatherThanMappingAgain() {
        // DexViewModel maps the dex once and passes the result back through here for every state
        // that follows. A mapper that ignored them would quietly undo that.
        val cards = DexState(entries = listOf(entry())).toUiModel().entries

        val ui = DexState(entries = listOf(entry(slug = "ignored"))).toUiModel(cards)

        assertSame(cards, ui.entries)
    }

    @Test
    fun toUiModel_carriesLoadingAndErrorThrough() {
        assertEquals(true, DexState(isLoading = true).toUiModel().isLoading)
        assertNotNull(DexState(error = AppError.Unexpected(IllegalStateException())).toUiModel().error)
    }

    @Test
    fun toUiModel_hasNoPrefetchLineBeforeTheRunStarts() {
        assertNull(DexState().toUiModel().prefetch)
    }
}
