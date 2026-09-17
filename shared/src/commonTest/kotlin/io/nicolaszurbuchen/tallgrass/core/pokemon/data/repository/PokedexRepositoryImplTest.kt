package io.nicolaszurbuchen.tallgrass.core.pokemon.data.repository

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.DexLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.PokemonDetailLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
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
                        slug = "vulpix-alola",
                        name = "Alolan Vulpix",
                        formLabel = "Alolan Form",
                        isDefault = false,
                        artworkUrl = "https://example.invalid/10103.png",
                        height = 6,
                        weight = 99,
                        primaryType = PokemonType.ICE,
                        secondaryType = null,
                        stats = PokemonStats(38, 41, 40, 50, 65, 65),
                    ),
                ),
        )

    private fun repository() =
        PokedexRepositoryImpl(
            dexLocalDataSource = StubDexLocalDataSource(listOf(entry)),
            detailLocalDataSource = StubDetailLocalDataSource(mapOf("vulpix-alola" to detail)),
        )

    @Test
    fun dexEntries_readsFromTheLocalSourceAndDoesNotReshape() =
        runTest {
            assertEquals(listOf(entry), repository().dexEntries())
        }

    @Test
    fun pokemonDetail_readsFromTheLocalSourceAndDoesNotReshape() =
        runTest {
            assertEquals(detail, repository().pokemonDetail("vulpix-alola"))
        }

    @Test
    fun pokemonDetail_passesAMissingSlugStraightThroughAsNull() =
        runTest {
            assertNull(repository().pokemonDetail("missingno"))
        }

    private class StubDexLocalDataSource(
        private val entries: List<DexEntry>,
    ) : DexLocalDataSource {
        override suspend fun dexEntries(): List<DexEntry> = entries
    }

    private class StubDetailLocalDataSource(
        private val details: Map<String, PokemonDetail>,
    ) : PokemonDetailLocalDataSource {
        override suspend fun detail(variantSlug: String): PokemonDetail? = details[variantSlug]
    }
}
