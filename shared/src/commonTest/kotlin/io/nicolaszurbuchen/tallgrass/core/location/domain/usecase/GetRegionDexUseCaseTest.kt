package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetRegionDexUseCaseTest {
    private val kantonianVulpix =
        DexEntry(
            slug = "vulpix",
            dexNumber = 37,
            name = "Vulpix",
            formLabel = null,
            artworkUrl = "vulpix.png",
            primaryType = PokemonType.FIRE,
            secondaryType = null,
        )

    private val alolanVulpix =
        kantonianVulpix.copy(slug = "vulpix-alola", artworkUrl = "vulpix-alola.png", primaryType = PokemonType.ICE)

    private val dexes = mapOf("kanto" to listOf(kantonianVulpix), "alola" to listOf(alolanVulpix))

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetRegionDexUseCase(FakeLocationRepository(dexes = dexes))

            assertEquals(listOf(kantonianVulpix), useCase("kanto"))
        }

    @Test
    fun invoke_returnsTheFormNativeToTheRegionAsking() =
        runTest {
            // The same Dex number resolving to two different forms is the whole of #5's correction,
            // and it is why a regional dex cannot be read off the species.
            val useCase = GetRegionDexUseCase(FakeLocationRepository(dexes = dexes))

            assertEquals("vulpix", useCase("kanto").single().slug)
            assertEquals("vulpix-alola", useCase("alola").single().slug)
            assertEquals(37, useCase("alola").single().dexNumber)
        }

    @Test
    fun invoke_returnsEmptyForARegionWithNoDex() =
        runTest {
            // Orre, which upstream has no regional dex for. Empty is the answer rather than a gap.
            val useCase = GetRegionDexUseCase(FakeLocationRepository(dexes = dexes))

            assertEquals(emptyList(), useCase("orre"))
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetRegionDexUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("kanto") }
        }
}
