package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EvYield
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonSpecies
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.GenderUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_attack
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_defense
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutUiMapperTest {
    private fun species(
        genderRate: Int = 1,
        eggGroups: List<EggGroup> = listOf(EggGroup.MONSTER, EggGroup.DRAGON),
        growthRate: GrowthRate = GrowthRate.MEDIUM_SLOW,
    ) = PokemonSpecies(
        dexNumber = 6,
        name = "Charizard",
        genus = "Flame Pokémon",
        genderRate = genderRate,
        captureRate = 45,
        hatchCounter = 20,
        growthRate = growthRate,
        eggGroups = eggGroups,
    )

    private fun variant(
        height: Int = 17,
        weight: Int = 905,
        baseExperience: Int? = 240,
        evYield: EvYield? = EvYield(hp = 0, attack = 0, defense = 0, specialAttack = 3, specialDefense = 0, speed = 0),
    ) = PokemonVariant(
        slug = "charizard",
        name = "Charizard",
        formLabel = null,
        formKind = FormKind.NONE,
        isDefault = true,
        artworkUrl = "",
        height = height,
        weight = weight,
        primaryType = PokemonType.FIRE,
        secondaryType = PokemonType.FLYING,
        stats = PokemonStats(78, 84, 78, 109, 85, 100),
        baseExperience = baseExperience,
        evYield = evYield,
    )

    private fun argsOf(text: UiText): List<Any> = (text as UiText.Resource).args

    /** A composed line flattened to what it is made of: literals as themselves, names as resources. */
    private fun partsOf(text: UiText): List<Any> =
        (text as UiText.Composite).parts.map { part ->
            when (part) {
                is UiText.Raw -> part.value
                is UiText.Resource -> part.id
                is UiText.Composite -> error("Nothing nests a composite here")
            }
        }

    private fun splitOf(genderRate: Int): GenderUiModel.Split =
        species(genderRate = genderRate).toAboutUiModel(variant()).gender as GenderUiModel.Split

    @Test
    fun toAboutUiModel_turnsDecimetresIntoMetresAndHectogramsIntoKilograms() {
        val about = species().toAboutUiModel(variant())

        assertEquals(listOf("1.7"), argsOf(about.heightText))
        assertEquals(listOf("90.5"), argsOf(about.weightText))
    }

    @Test
    fun toAboutUiModel_keepsTheTrailingZeroRatherThanRoundingAwayTheDecimal() {
        // Ten decimetres is a metre, and "1.0 m" is the honest reading of a value stored in tenths.
        assertEquals(listOf("1.0"), argsOf(species().toAboutUiModel(variant(height = 10)).heightText))
    }

    @Test
    fun toAboutUiModel_splitsTheEighthsIntoAPercentageWithOneDecimal() {
        // A gender rate of 1 is one eighth female, which is 12.5% -- a number no integer percentage
        // can express, and the case that makes this arithmetic worth pinning.
        val split = splitOf(genderRate = 1)

        assertEquals(listOf("87.5"), argsOf(split.maleText))
        assertEquals(listOf("12.5"), argsOf(split.femaleText))
    }

    @Test
    fun toAboutUiModel_dropsTheDecimalWhenTheShareIsWhole() {
        assertEquals(listOf("50"), argsOf(splitOf(genderRate = 4).maleText))
        assertEquals(listOf("50"), argsOf(splitOf(genderRate = 4).femaleText))
    }

    @Test
    fun toAboutUiModel_keepsTheEmptyShareRatherThanDroppingTheSymbol() {
        // An all-male species is still a split, and drawing the female side as 0% says more than
        // leaving the symbol off the row.
        assertEquals(listOf("100"), argsOf(splitOf(genderRate = 0).maleText))
        assertEquals(listOf("0"), argsOf(splitOf(genderRate = 0).femaleText))
    }

    @Test
    fun toAboutUiModel_saysGenderlessRatherThanDividingByNothing() {
        // Upstream writes -1 for a genderless species, which is a third case rather than a share.
        assertEquals(GenderUiModel.Genderless, species(genderRate = -1).toAboutUiModel(variant()).gender)
    }

    @Test
    fun toAboutUiModel_namesTheEggGroupsAsThePlayerKnowsThem() {
        val about = species(eggGroups = listOf(EggGroup.GRASS, EggGroup.FIELD)).toAboutUiModel(variant())

        assertEquals(UiText.Raw("Grass, Field"), about.eggGroupsText)
    }

    @Test
    fun toAboutUiModel_namesTheOddGrowthCurves() {
        val about = species(growthRate = GrowthRate.ERRATIC).toAboutUiModel(variant())

        assertEquals(UiText.Raw("Erratic"), about.growthText)
    }

    @Test
    fun toAboutUiModel_countsTheEggCycleInCycles() {
        assertEquals(listOf("20"), argsOf(species().toAboutUiModel(variant()).eggCycleText))
    }

    @Test
    fun toAboutUiModel_namesOnlyTheStatsAFormAwards() {
        // Charizard yields three Special Attack and nothing else. The four stats it awards nothing
        // against are what the line is mostly made of if they are not dropped.
        val about = species().toAboutUiModel(variant())

        assertEquals(listOf("3 ", Res.string.pokedex_detail_stat_special_attack), partsOf(about.evYieldText!!))
    }

    @Test
    fun toAboutUiModel_separatesTheAwardsAndKeepsThemInTheOrderTheStatsTabUses() {
        // Butterfree, which is the ordinary two-stat case: Special Attack comes before Special
        // Defense because that is the order the bars are drawn in, not because of its figure.
        val butterfree = EvYield(hp = 0, attack = 0, defense = 0, specialAttack = 2, specialDefense = 1, speed = 0)

        assertEquals(
            listOf(
                "2 ",
                Res.string.pokedex_detail_stat_special_attack,
                ", ",
                "1 ",
                Res.string.pokedex_detail_stat_special_defense,
            ),
            partsOf(species().toAboutUiModel(variant(evYield = butterfree)).evYieldText!!),
        )
    }

    @Test
    fun toAboutUiModel_leavesBothTrainingFiguresOutWhenUpstreamHasNotCostedTheForm() {
        // The 49 Legends Z-A Megas. Absent rather than zero: a Pokemon worth no experience and
        // awarding no effort is not a thing, so the rows go rather than reporting one.
        val about = species().toAboutUiModel(variant(baseExperience = null, evYield = null))

        assertEquals(null, about.evYieldText)
        assertEquals(null, about.baseExperienceText)
    }

    @Test
    fun toAboutUiModel_drawsBaseExperienceAsTheFigureItIs() {
        assertEquals(UiText.Raw("240"), species().toAboutUiModel(variant()).baseExperienceText)
    }
}
