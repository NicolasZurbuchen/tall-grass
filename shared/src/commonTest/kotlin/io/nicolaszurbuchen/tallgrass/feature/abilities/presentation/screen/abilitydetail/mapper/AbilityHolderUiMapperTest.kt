package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AbilityHolderUiMapperTest {
    @Test
    fun theCardIsKeyedAndTintedByTheVariantRatherThanTheCard() {
        // The grid shows forms, so Alolan Sandshrew is its own cell with its own colour. Which dex
        // card it opens through is the Store's business, not the card's.
        val holder = AbilityFixtures.gastly.toUiModel()

        assertEquals("gastly", holder.slug)
        assertEquals("Gastly", holder.name)
        assertEquals(TypeUiModel.GHOST, holder.tint)
    }

    @Test
    fun aHiddenHolderSaysSo() {
        assertEquals(true, AbilityFixtures.vibrava.toUiModel().hiddenText != null)
    }

    @Test
    fun anOrdinaryHolderSaysNothingRatherThanSayingNormal() {
        // There is no word for the ordinary case worth the line: the reader already knows every
        // Pokemon in this grid has the ability.
        assertNull(AbilityFixtures.gastly.toUiModel().hiddenText)
    }
}
