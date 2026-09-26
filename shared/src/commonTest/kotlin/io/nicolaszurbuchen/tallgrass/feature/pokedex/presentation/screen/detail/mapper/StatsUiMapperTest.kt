package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EvYield
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatsUiMapperTest {
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
    fun toStatsUiModel_labelsEachFactorTheWayTheGamesWriteIt() {
        val matchups =
            listOf(
                TypeMatchup(PokemonType.ROCK, 400),
                TypeMatchup(PokemonType.WATER, 200),
                TypeMatchup(PokemonType.FIGHTING, 50),
                TypeMatchup(PokemonType.GRASS, 25),
                TypeMatchup(PokemonType.GROUND, 0),
            )

        val labels = variant().toStatsUiModel(matchups).matchups.map { it.factorText }

        assertEquals(listOf("×4", "×2", "½", "¼", "0"), labels)
    }

    @Test
    fun toStatsUiModel_colorsAMatchupByTheAttackingType() {
        val chips = variant().toStatsUiModel(listOf(TypeMatchup(PokemonType.ROCK, 400)))

        assertEquals(TypeUiModel.ROCK.color, chips.matchups.single().typeColor)
        assertEquals("Rock", chips.matchups.single().typeLabel)
    }
}
