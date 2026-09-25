package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VariantAbilityUiMapperTest {
    @Test
    fun theRowCarriesTheNameAndTheShortEffectUnchanged() {
        val row = AbilityFixtures.blaze.toUiModel()

        assertEquals("blaze", row.slug)
        assertEquals("Blaze", row.name)
        assertEquals(AbilityFixtures.blaze.shortEffect, row.shortEffect)
    }

    @Test
    fun theInitialMatchesTheOneTheAbilitiesListDraws() {
        // The same rule as AbilityUiMapper's, so a row here and a card there are the same thing seen
        // twice rather than two things that happen to look alike.
        assertEquals("B", AbilityFixtures.blaze.toUiModel().initial)
        assertEquals("S", AbilityFixtures.solarPower.toUiModel().initial)
    }

    @Test
    fun aHiddenAbilitySaysSoAndAnOrdinaryOneSaysNothing() {
        assertEquals(true, AbilityFixtures.solarPower.toUiModel().hiddenText != null)
        assertNull(AbilityFixtures.blaze.toUiModel().hiddenText)
    }
}
