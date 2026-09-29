package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EvYield
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_dynamax_hp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StatsUiMapperTest {
    // One of each of the five the chart can produce, in no particular order: the mapper's job is to
    // put them in one.
    private val everyKindOfMatchup =
        listOf(
            TypeMatchup(PokemonType.ROCK, 400),
            TypeMatchup(PokemonType.WATER, 200),
            TypeMatchup(PokemonType.FIGHTING, 50),
            TypeMatchup(PokemonType.GRASS, 25),
            TypeMatchup(PokemonType.GROUND, 0),
            TypeMatchup(PokemonType.ELECTRIC, 200),
        )

    private fun variant(stats: PokemonStats = PokemonStats(78, 84, 78, 109, 85, 100)) =
        PokemonVariant(
            slug = "charizard",
            name = "Charizard",
            formLabel = null,
            formKind = FormKind.NONE,
            isDefault = true,
            artworkUrl = "",
            height = 17,
            weight = 905,
            primaryType = PokemonType.FIRE,
            secondaryType = PokemonType.FLYING,
            stats = stats,
            baseExperience = 240,
            evYield = EvYield(hp = 0, attack = 0, defense = 0, specialAttack = 3, specialDefense = 0, speed = 0),
            gmaxMove = null,
        )

    @Test
    fun toStatsUiModel_drawsTheSixStatsInTheOrderEveryScreenUsesThem() {
        val bars = variant().toStatsUiModel(emptyList()).bars

        assertEquals(listOf("78", "84", "78", "109", "85", "100"), bars.map { it.valueText })
    }

    @Test
    fun toStatsUiModel_addsTheSixUp() {
        assertEquals("534", variant().toStatsUiModel(emptyList()).totalText)
    }

    @Test
    fun toStatsUiModel_addsTheTwoRangeColumnsUpOnTheTotalRow() {
        // Charizard's six bands summed. Arithmetic rather than a figure from the games: no Pokemon
        // reaches either end, because one nature cannot help all six stats and 510 EVs are a third of
        // the 1,512 the maxima assume. The reader adding the column up by hand gets the same number,
        // which is the whole reason the row has it.
        val ui = variant().toStatsUiModel(emptyList())

        assertEquals("1106", ui.totalMinText)
        assertEquals("1904", ui.totalMaxText)
        assertEquals(ui.bars.sumOf { it.minText.toInt() }.toString(), ui.totalMinText)
        assertEquals(ui.bars.sumOf { it.maxText.toInt() }.toString(), ui.totalMaxText)
    }

    @Test
    fun toStatsUiModel_scalesTheTotalBarBySixFullStats() {
        // The total's lane is the mean of the six above it. A stat is full at 160, so the total is
        // full at six times that, and the two can be read down the same column.
        val stats = variant(PokemonStats(hp = 80, attack = 80, defense = 80, specialAttack = 80, specialDefense = 80, speed = 80))

        assertEquals(0.5f, stats.toStatsUiModel(emptyList()).totalFraction)
    }

    @Test
    fun toStatsUiModel_clampsATotalThatWouldOverflowItsLane() {
        // No real Pokemon reaches 960, but the ceiling is a rendering promise rather than a fact
        // about the dataset, and a lane longer than itself is not a thing a Box can draw.
        val stats = variant(PokemonStats(hp = 255, attack = 255, defense = 255, specialAttack = 255, specialDefense = 255, speed = 255))

        assertEquals(1f, stats.toStatsUiModel(emptyList()).totalFraction)
    }

    @Test
    fun toStatsUiModel_clampsABarThatWouldOverflowItsLane() {
        // Blissey's 255 HP is well past the value a full bar stands for. Without the clamp the bar
        // would be asked to be longer than the lane it sits in.
        val stats = variant(PokemonStats(hp = 255, attack = 10, defense = 10, specialAttack = 75, specialDefense = 135, speed = 55))

        val bars = stats.toStatsUiModel(emptyList()).bars

        assertEquals(1f, bars.first().fraction)
        assertTrue(bars.all { it.fraction <= 1f })
    }

    @Test
    fun toStatsUiModel_scalesABarBelowTheCeilingProportionally() {
        val stats = variant(PokemonStats(hp = 80, attack = 40, defense = 0, specialAttack = 0, specialDefense = 0, speed = 0))

        val bars = stats.toStatsUiModel(emptyList()).bars

        assertEquals(0.5f, bars[0].fraction)
        assertEquals(0.25f, bars[1].fraction)
        assertEquals(0f, bars[2].fraction)
    }

    @Test
    fun toStatsUiModel_saysWhatDynamaxDoesToAGigantamaxFormsHitPoints() {
        // Charizard's HP at level 100 is 266 to 360, and Dynamax Level 10 doubles it. The note is a
        // sentence under the table rather than a column in it, because Dynamaxing multiplies the HP
        // the Pokemon already has and there is nothing to put beside a base stat.
        val gmax = variant().copy(gmaxMove = "g-max-wildfire").toStatsUiModel(emptyList())

        assertEquals(UiText.Resource(Res.string.pokedex_detail_dynamax_hp, listOf(532, 720)), gmax.dynamaxHpText)
    }

    @Test
    fun toStatsUiModel_saysNothingAboutDynamaxOnAFormThatCannotGigantamax() {
        // Every Pokemon in Sword and Shield can Dynamax, so this is the narrower of two true things:
        // it is shown where a reader is already asking what the form does.
        assertNull(variant().toStatsUiModel(emptyList()).dynamaxHpText)
    }

    @Test
    fun toStatsUiModel_sortsTheChartIntoWhatGetsThroughAndWhatBouncesOff() {
        val ui = variant().toStatsUiModel(everyKindOfMatchup)

        assertEquals(listOf("×2", "×4"), ui.weaknesses.map { it.factorText })
        assertEquals(listOf("0", "¼", "½"), ui.resistances.map { it.factorText })
    }

    @Test
    fun toStatsUiModel_saysEachFactorOnceRatherThanOnEveryChip() {
        // The reason for the split: "×2" printed four times in a row on most Pokemon, which is the
        // same word doing the work of a heading four times over.
        val doubled = variant().toStatsUiModel(everyKindOfMatchup).weaknesses.first { it.factorText == "×2" }

        assertEquals(listOf("Water", "Electric"), doubled.types.map { it.typeLabel })
    }

    @Test
    fun toStatsUiModel_leavesOutAFactorNothingHitsThisPokemonFor() {
        // Eelektross is weak to nothing at all, and most Pokemon are missing two or three of the
        // five. A row with no chips in it is a question nobody asked.
        val ui = variant().toStatsUiModel(listOf(TypeMatchup(PokemonType.GRASS, 25)))

        assertTrue(ui.weaknesses.isEmpty())
        assertEquals(listOf("¼"), ui.resistances.map { it.factorText })
    }

    @Test
    fun toStatsUiModel_colorsAMatchupByTheAttackingType() {
        val chip = variant().toStatsUiModel(listOf(TypeMatchup(PokemonType.ROCK, 400))).weaknesses.single().types.single()

        assertEquals(TypeUiModel.ROCK.color, chip.typeColor)
        assertEquals("Rock", chip.typeLabel)
    }
}
