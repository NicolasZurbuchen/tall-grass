package io.nicolaszurbuchen.tallgrass.core.pokemon.data.repository

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PokedexRepositoryImplTest {
    private val entry =
        DexEntry(
            slug = "vulpix-alola",
            dexNumber = 37,
            name = "Alolan Vulpix",
            formLabel = "Alolan Form",
            artworkUrl = "https://example.invalid/10103.png",
            primaryType = PokemonType.ICE,
            secondaryType = null,
        )

    @Test
    fun dexEntries_readsFromTheLocalSourceAndDoesNotReshape() =
        runTest {
            val repository = PokedexRepositoryImpl(StubLocalDataSource(listOf(entry)))

            assertEquals(listOf(entry), repository.dexEntries())
        }

    private class StubLocalDataSource(
        private val entries: List<DexEntry>,
    ) : DexLocalDataSource {
        override suspend fun dexEntries(): List<DexEntry> = entries
    }
}
