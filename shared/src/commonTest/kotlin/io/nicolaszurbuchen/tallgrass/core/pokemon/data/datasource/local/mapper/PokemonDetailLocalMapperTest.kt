package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.SelectVariantDetails
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.Species
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.VariantStat
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PokemonDetailLocalMapperTest {
    private fun speciesRow(growthRate: String = "medium") =
        Species(
            dexNumber = 37,
            slug = "vulpix",
            name = "Vulpix",
            genus = "Fox Pokémon",
            generation = 1,
            genderRate = 6,
            captureRate = 190,
            hatchCounter = 20,
            growthRate = growthRate,
            isBaby = false,
            isLegendary = false,
            isMythical = false,
        )

    private fun variantRow(
        slug: String = "vulpix-alola",
        formLabel: String? = "Alolan Form",
        isDefault: Boolean = false,
        primaryType: String? = "ice",
        secondaryType: String? = null,
    ) = SelectVariantDetails(
        slug = slug,
        name = "Alolan Vulpix",
        formLabel = formLabel,
        isDefault = isDefault,
        height = 6,
        weight = 99,
        artworkUrl = "https://example.invalid/10103.png",
        primaryType = primaryType,
        secondaryType = secondaryType,
    )

    private fun statRows(
        slug: String,
        omit: String? = null,
    ) = listOf(
        "hp" to 38L,
        "attack" to 41L,
        "defense" to 40L,
        "special-attack" to 50L,
        "special-defense" to 65L,
        "speed" to 65L,
    ).filterNot { it.first == omit }
        .map { (statSlug, value) -> VariantStat(variantSlug = slug, statSlug = statSlug, baseStat = value) }

    private val stats = PokemonStats(hp = 38, attack = 41, defense = 40, specialAttack = 50, specialDefense = 65, speed = 65)

    @Test
    fun toDomain_readsTheSpeciesFieldsTheBreedingBlockDraws() {
        val species = speciesRow().toDomain(listOf("ground", "fairy"))

        assertEquals(37, species?.dexNumber)
        assertEquals(6, species?.genderRate)
        assertEquals(190, species?.captureRate)
        assertEquals(20, species?.hatchCounter)
        assertEquals(GrowthRate.MEDIUM_FAST, species?.growthRate)
        assertEquals(listOf(EggGroup.FIELD, EggGroup.FAIRY), species?.eggGroups)
    }

    @Test
    fun toDomain_dropsASpeciesWhoseGrowthRateIsUnknown() {
        assertNull(speciesRow(growthRate = "very-fast").toDomain(emptyList()))
    }

    @Test
    fun toDomain_dropsOnlyTheEggGroupItCannotRead() {
        // One unreadable group should not cost the species its breeding block.
        val species = speciesRow().toDomain(listOf("ground", "chimera"))

        assertEquals(listOf(EggGroup.FIELD), species?.eggGroups)
    }

    @Test
    fun toDomain_carriesTheVariantFieldsTheHeroAndTabsDraw() {
        val variant = variantRow(secondaryType = "fairy").toDomain(stats)

        assertEquals("vulpix-alola", variant?.slug)
        assertEquals("Alolan Form", variant?.formLabel)
        assertEquals(PokemonType.ICE, variant?.primaryType)
        assertEquals(PokemonType.FAIRY, variant?.secondaryType)
        assertEquals(stats, variant?.stats)
    }

    @Test
    fun toDomain_keepsHeightAndWeightInUpstreamUnits() {
        // Decimetres and hectograms. Converting here would put the same division in every caller.
        val variant = variantRow().toDomain(stats)

        assertEquals(6, variant?.height)
        assertEquals(99, variant?.weight)
    }

    @Test
    fun toDomain_dropsAVariantWhosePrimaryTypeIsUnknown() {
        assertNull(variantRow(primaryType = "stellar").toDomain(stats))
        assertNull(variantRow(primaryType = null).toDomain(stats))
    }

    @Test
    fun toStatsByVariantDomain_gathersTheSixRowsOfEachVariant() {
        val byVariant = (statRows("vulpix") + statRows("vulpix-alola")).toStatsByVariantDomain()

        assertEquals(setOf("vulpix", "vulpix-alola"), byVariant.keys)
        assertEquals(stats, byVariant["vulpix"])
    }

    @Test
    fun toStatsByVariantDomain_leavesOutAVariantMissingOneOfTheSix() {
        // A stat bar drawn at zero is indistinguishable from a real answer, so an incomplete set
        // costs the variant its place rather than being filled in.
        val byVariant = (statRows("vulpix") + statRows("vulpix-alola", omit = "speed")).toStatsByVariantDomain()

        assertEquals(setOf("vulpix"), byVariant.keys)
    }

    @Test
    fun toStatsByVariantDomain_isEmptyWhenThereAreNoRows() {
        assertTrue(emptyList<VariantStat>().toStatsByVariantDomain().isEmpty())
    }
}
