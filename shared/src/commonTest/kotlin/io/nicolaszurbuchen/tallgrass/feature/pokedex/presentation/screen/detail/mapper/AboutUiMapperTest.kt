package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonSpecies
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.detail_genderless
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
    ) = PokemonVariant(
        slug = "charizard",
        name = "Charizard",
        formLabel = null,
        isDefault = true,
        artworkUrl = "",
        height = height,
        weight = weight,
        primaryType = PokemonType.FIRE,
        secondaryType = PokemonType.FLYING,
        stats = PokemonStats(78, 84, 78, 109, 85, 100),
    )

    private fun argsOf(text: UiText): List<Any> = (text as UiText.Resource).args

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
        assertEquals(listOf("87.5", "12.5"), argsOf(species(genderRate = 1).toAboutUiModel(variant()).genderText))
    }

    @Test
    fun toAboutUiModel_dropsTheDecimalWhenTheShareIsWhole() {
        assertEquals(listOf("50", "50"), argsOf(species(genderRate = 4).toAboutUiModel(variant()).genderText))
        assertEquals(listOf("100", "0"), argsOf(species(genderRate = 0).toAboutUiModel(variant()).genderText))
    }

    @Test
    fun toAboutUiModel_saysGenderlessRatherThanDividingByNothing() {
        // Upstream writes -1 for a genderless species, which is a third case rather than a share.
        assertEquals(
            UiText.Resource(Res.string.detail_genderless),
            species(genderRate = -1).toAboutUiModel(variant()).genderText,
        )
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
    fun toAboutUiModel_carriesTheTrainingNumbersAsTheyAre() {
        val about = species().toAboutUiModel(variant())

        assertEquals(UiText.Raw("45"), about.catchRateText)
        assertEquals(listOf("20"), argsOf(about.eggCycleText))
    }
}
