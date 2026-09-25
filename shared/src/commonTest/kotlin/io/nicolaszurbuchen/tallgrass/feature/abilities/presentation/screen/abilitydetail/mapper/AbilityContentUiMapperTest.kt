package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilityContentUiMapperTest {
    @Test
    fun theNameAndTheShortEffectAreCarriedUnchanged() {
        val content = AbilityFixtures.levitateDetail.toUiModel()

        assertEquals("Levitate", content.name)
        assertEquals("Evades Ground moves.", content.shortEffect)
    }

    @Test
    fun theGenerationIsFormattedByAResourceRatherThanBuiltHere() {
        // A raw string here would be an English sentence compiled into the mapper.
        val generation = AbilityFixtures.levitateDetail.toUiModel().generationText

        assertTrue(generation is UiText.Resource)
        assertEquals(listOf(3), generation.args)
    }

    @Test
    fun theLongEffectIsKeptWhenItSaysMoreThanTheShortOne() {
        val content = AbilityFixtures.levitateDetail.toUiModel()

        assertEquals(AbilityFixtures.levitateDetail.effect, content.effect)
    }

    @Test
    fun theLongEffectIsDroppedWhenItRepeatsTheShortOneVerbatim() {
        // 46 of the 314 are written that way upstream, and an "In depth" heading over a repeat of the
        // line above it reads as a rendering fault rather than as an ability with little to say.
        val content = AbilityFixtures.stenchDetail.toUiModel()

        assertEquals(AbilityFixtures.stenchDetail.shortEffect, content.shortEffect)
        assertNull(content.effect)
    }
}
