package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DexReducerTest {
    private val reduce = DexStoreFactory.ReducerImpl

    private val entry =
        DexEntry(
            slug = "bulbasaur",
            dexNumber = 1,
            name = "Bulbasaur",
            formLabel = null,
            artworkUrl = "https://example.invalid/1.png",
            primaryType = PokemonType.GRASS,
            secondaryType = null,
        )

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            // Otherwise a retry would show the spinner and the old error banner at once.
            val state = DexState(error = AppError.Unexpected(IllegalStateException())).reduce(DexMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun entriesLoaded_stopsLoadingAndHoldsTheEntries() =
        with(reduce) {
            val state = DexState(isLoading = true).reduce(DexMessage.EntriesLoaded(listOf(entry)))

            assertEquals(listOf(entry), state.entries)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun loadFailed_stopsLoadingAndKeepsWhatWasAlreadyShown() =
        with(reduce) {
            // A failed refresh over a populated grid should not empty it.
            val loaded = DexState(entries = listOf(entry), isLoading = false)

            val state = loaded.reduce(DexMessage.LoadFailed(AppError.Unexpected(IllegalStateException())))

            assertEquals(listOf(entry), state.entries)
            assertEquals(false, state.isLoading)
            assertTrue(state.error != null)
        }
}
