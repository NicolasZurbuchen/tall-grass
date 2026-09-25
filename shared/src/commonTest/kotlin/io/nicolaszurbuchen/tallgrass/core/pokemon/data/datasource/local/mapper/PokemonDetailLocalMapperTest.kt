package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.SelectVariantDetails
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.Species
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.VariantStat
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EvYield
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
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
        formKind: String = "REGIONAL",
        isDefault: Boolean = false,
        primaryType: String? = "ice",
        secondaryType: String? = null,
        baseExperience: Long? = 60,
    ) = SelectVariantDetails(
        slug = slug,
        name = "Alolan Vulpix",
        formLabel = formLabel,
        formKind = formKind,
        isDefault = isDefault,
        height = 6,
        weight = 99,
        baseExperience = baseExperience,
        artworkUrl = "https://example.invalid/10103.png",
        primaryType = primaryType,
        secondaryType = secondaryType,
    )

    /** Vulpix: 38/41/40/50/65/65, and one Speed for whoever beats it. */
    private fun statRows(
        slug: String,
        omit: String? = null,
        uncosted: Boolean = false,
    ) = listOf(
        Triple("hp", 38L, 0L),
        Triple("attack", 41L, 0L),
        Triple("defense", 40L, 0L),
        Triple("special-attack", 50L, 0L),
        Triple("special-defense", 65L, 0L),
        Triple("speed", 65L, 1L),
    ).filterNot { it.first == omit }
        .map { (statSlug, base, effort) ->
            VariantStat(
                variantSlug = slug,
                statSlug = statSlug,
                baseStat = base,
                effort = if (uncosted) 0L else effort,
            )
        }

    private val stats = PokemonStats(hp = 38, attack = 41, defense = 40, specialAttack = 50, specialDefense = 65, speed = 65)

    private val evYield = EvYield(hp = 0, attack = 0, defense = 0, specialAttack = 0, specialDefense = 0, speed = 1)

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
        val variant = variantRow(secondaryType = "fairy").toDomain(stats, evYield)

        assertEquals("vulpix-alola", variant?.slug)
        assertEquals("Alolan Form", variant?.formLabel)
        assertEquals(PokemonType.ICE, variant?.primaryType)
        assertEquals(PokemonType.FAIRY, variant?.secondaryType)
        assertEquals(stats, variant?.stats)
    }

    @Test
    fun toDomain_keepsHeightAndWeightInUpstreamUnits() {
        // Decimetres and hectograms. Converting here would put the same division in every caller.
        val variant = variantRow().toDomain(stats, evYield)

        assertEquals(6, variant?.height)
        assertEquals(99, variant?.weight)
    }

    @Test
    fun toDomain_dropsAVariantWhosePrimaryTypeIsUnknown() {
        assertNull(variantRow(primaryType = "stellar").toDomain(stats, evYield))
        assertNull(variantRow(primaryType = null).toDomain(stats, evYield))
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

    @Test
    fun toEvYieldByVariantDomain_readsTheAwardOffTheSameRowsAsTheBaseStats() {
        val byVariant = (statRows("vulpix") + statRows("vulpix-alola")).toEvYieldByVariantDomain()

        assertEquals(setOf("vulpix", "vulpix-alola"), byVariant.keys)
        assertEquals(evYield, byVariant["vulpix"])
    }

    @Test
    fun toEvYieldByVariantDomain_leavesOutAFormThatAwardsNothingAtAll() {
        // The 49 Legends Z-A Megas: upstream has written no effort against any of their six stats,
        // which is a figure it has not decided rather than a Pokemon worth no effort. Every costed
        // form awards between one and three, so all six at zero is the whole test.
        val byVariant = (statRows("vulpix") + statRows("clefable-mega", uncosted = true)).toEvYieldByVariantDomain()

        assertEquals(setOf("vulpix"), byVariant.keys)
    }

    @Test
    fun toEvYieldByVariantDomain_leavesOutAVariantMissingOneOfTheSix() {
        // Same rule as the base stats and for a sharper reason: a yield that says nothing about
        // Speed because the row was not read is a different claim from one that awards no Speed.
        val byVariant = (statRows("vulpix") + statRows("vulpix-alola", omit = "speed")).toEvYieldByVariantDomain()

        assertEquals(setOf("vulpix"), byVariant.keys)
    }

    @Test
    fun toDomain_carriesTheTrainingFiguresTheAboutTabDraws() {
        val variant = variantRow().toDomain(stats, evYield)

        assertEquals(60, variant?.baseExperience)
        assertEquals(evYield, variant?.evYield)
    }

    @Test
    fun toDomain_keepsAnUncostedFormRatherThanDroppingIt() {
        // A form upstream has not costed is still a form: it has a name, artwork, types and stats,
        // and only the two training rows are missing.
        val variant = variantRow(baseExperience = null).toDomain(stats, evYield = null)

        assertEquals("vulpix-alola", variant?.slug)
        assertNull(variant?.baseExperience)
        assertNull(variant?.evYield)
    }

    @Test
    fun toDomain_readsTheFormKindTheSwitcherFiltersOn() {
        assertEquals(FormKind.REGIONAL, variantRow(formKind = "REGIONAL").toDomain(stats, evYield)?.formKind)
        assertEquals(FormKind.COSMETIC, variantRow(formKind = "COSMETIC").toDomain(stats, evYield)?.formKind)
    }

    @Test
    fun toDomain_showsAFormWhoseKindThisBuildDoesNotKnow() {
        // The opposite of how an unknown type is read, and deliberately so: an unrecognised kind
        // means this build cannot say how the form differs, not that the form is not real. Dropping
        // it -- or calling it cosmetic -- would lose a Pokemon from the switcher over a label.
        assertEquals(FormKind.ALTERNATE, variantRow(formKind = "PARADOX").toDomain(stats, evYield)?.formKind)
    }
}
