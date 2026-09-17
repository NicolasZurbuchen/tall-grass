package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.fake.FakePokedexRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonSpecies
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetPokemonDetailUseCaseTest {
    private val detail =
        PokemonDetail(
            species =
                PokemonSpecies(
                    dexNumber = 37,
                    name = "Vulpix",
                    genus = "Fox Pokémon",
                    genderRate = 6,
                    captureRate = 190,
                    hatchCounter = 20,
                    growthRate = GrowthRate.MEDIUM_FAST,
                    eggGroups = listOf(EggGroup.FIELD),
                ),
            variants =
                listOf(
                    PokemonVariant(
                        slug = "vulpix",
                        name = "Vulpix",
                        formLabel = null,
                        isDefault = true,
                        artworkUrl = "https://example.invalid/37.png",
                        height = 6,
                        weight = 99,
                        primaryType = PokemonType.FIRE,
                        secondaryType = null,
                        stats = PokemonStats(38, 41, 40, 50, 65, 65),
                    ),
                ),
        )

    @Test
    fun invoke_readsTheDetailForTheSlugItIsGiven() =
        runTest {
            val useCase = GetPokemonDetailUseCase(FakePokedexRepository(details = mapOf("vulpix" to detail)))

            assertEquals(detail, useCase("vulpix"))
        }

    @Test
    fun invoke_returnsNullForASlugTheDatasetDoesNotHave() =
        runTest {
            // A saved back stack can outlive the dataset it was built against. Null here is what the
            // Store turns into "not in this Pokedex" rather than a storage failure.
            val useCase = GetPokemonDetailUseCase(FakePokedexRepository(details = mapOf("vulpix" to detail)))

            assertNull(useCase("missingno"))
        }
}
