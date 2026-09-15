package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.fake.FakePokedexRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetDexEntriesUseCaseTest {
    private val bulbasaur =
        DexEntry(
            slug = "bulbasaur",
            dexNumber = 1,
            name = "Bulbasaur",
            formLabel = null,
            artworkUrl = "https://example.invalid/1.png",
            primaryType = PokemonType.GRASS,
            secondaryType = PokemonType.POISON,
        )

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetDexEntriesUseCase(FakePokedexRepository(entries = listOf(bulbasaur)))

            assertEquals(listOf(bulbasaur), useCase())
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningEmpty() =
        runTest {
            // An empty dex and a broken dex look identical to the grid, so the use case must not
            // flatten one into the other.
            val useCase = GetDexEntriesUseCase(FakePokedexRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase() }
        }
}
