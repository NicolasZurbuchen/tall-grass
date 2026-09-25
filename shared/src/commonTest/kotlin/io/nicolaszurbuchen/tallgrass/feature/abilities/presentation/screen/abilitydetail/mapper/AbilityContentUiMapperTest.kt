package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AbilityContentUiMapperTest {
    private val holders = listOf(AbilityFixtures.gastly, AbilityFixtures.vibrava)

    @Test
    fun theThreeFiguresAreGenerationHoldersAndHiddenHolders() {
        val content = AbilityFixtures.levitateDetail.toUiModel(holders)

        assertEquals(listOf("3", "2", "1"), content.stats.map { it.valueText })
    }

    @Test
    fun theHiddenFigureCountsOnlyTheHoldersThatHaveItHidden() {
        // "96 Pokemon, 4 of them hidden" and "96 Pokemon, all of them hidden" are different answers
        // to the question someone opened the screen with, so this is not the holder count again.
        val allHidden = listOf(AbilityFixtures.vibrava)

        assertEquals("1", AbilityFixtures.levitateDetail.toUiModel(allHidden).stats.last().valueText)
        assertEquals("0", AbilityFixtures.levitateDetail.toUiModel(listOf(AbilityFixtures.gastly)).stats.last().valueText)
    }

    @Test
    fun anAbilityNothingHasStillReadsZeroRatherThanBlank() {
        val content = AbilityFixtures.levitateDetail.toUiModel(emptyList())

        assertEquals(listOf("3", "0", "0"), content.stats.map { it.valueText })
    }

    @Test
    fun theLongEffectIsKeptWhenItSaysMoreThanTheShortOne() {
        val content = AbilityFixtures.levitateDetail.toUiModel(holders)

        assertEquals("Evades Ground moves.", content.shortEffect)
        assertEquals(AbilityFixtures.levitateDetail.effect, content.effect)
    }

    @Test
    fun theLongEffectIsDroppedWhenItRepeatsTheShortOneVerbatim() {
        // 46 of the 314 are written that way upstream, and an "In depth" heading over a repeat of the
        // line above it reads as a rendering fault rather than as an ability with little to say.
        val content = AbilityFixtures.stenchDetail.toUiModel(holders)

        assertEquals(AbilityFixtures.stenchDetail.shortEffect, content.shortEffect)
        assertNull(content.effect)
    }
}
