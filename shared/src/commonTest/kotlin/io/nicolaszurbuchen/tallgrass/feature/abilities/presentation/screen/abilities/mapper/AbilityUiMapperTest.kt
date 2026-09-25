package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AbilityUiMapperTest {
    @Test
    fun theCardCarriesTheNameAndTheShortEffectUnchanged() {
        val card = AbilityFixtures.levitate.toUiModel()

        assertEquals("levitate", card.slug)
        assertEquals("Levitate", card.name)
        assertEquals("Evades Ground moves.", card.shortEffect)
    }

    @Test
    fun theInitialIsTheFirstLetterOfTheName() {
        assertEquals("L", AbilityFixtures.levitate.toUiModel().initial)
        assertEquals("A", AbilityFixtures.adaptability.toUiModel().initial)
    }

    @Test
    fun theInitialIsUppercasedWhateverTheNameDoes() {
        // Upstream capitalises all 314 today, so this pins the rule rather than a case that exists:
        // the tile is a display of the letter rather than the first character of a word.
        val lowercase = Ability(slug = "x", name = "levitate", generation = 3, shortEffect = "")

        assertEquals("L", lowercase.toUiModel().initial)
    }

    @Test
    fun theGenerationIsFormattedByAResourceRatherThanBuiltHere() {
        // A raw string here would be an English sentence compiled into the mapper.
        val generation = AbilityFixtures.levitate.toUiModel().generationText

        assertTrue(generation is UiText.Resource)
        assertEquals(listOf(3), generation.args)
    }
}
