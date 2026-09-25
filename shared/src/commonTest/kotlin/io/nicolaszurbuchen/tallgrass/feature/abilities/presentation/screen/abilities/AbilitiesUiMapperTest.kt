package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilitiesUiMapperTest {
    @Test
    fun theStateMapsItsOwnCardsWhenNoneArePassedIn() {
        val state = AbilitiesState(isLoading = false, abilities = listOf(AbilityFixtures.levitate))

        val ui = state.toUiModel()

        assertEquals(1, ui.abilities.size)
        assertEquals("Levitate", ui.abilities.first().name)
    }

    @Test
    fun cardsPassedInWinOverTheStatesOwn() {
        // This is the whole point of the parameter: AbilitiesViewModel maps the cards once per list
        // rather than once per state, and passes the result back in.
        val state = AbilitiesState(isLoading = false, abilities = listOf(AbilityFixtures.levitate))
        val cached =
            listOf(
                AbilityUiModel(
                    slug = "cached",
                    name = "Cached",
                    initial = "C",
                    generationText = UiText.Raw("Gen 1"),
                    shortEffect = "",
                ),
            )

        assertEquals(cached, state.toUiModel(cached).abilities)
    }

    @Test
    fun anErrorIsCarriedAcrossAndAnAbsentOneStaysNull() {
        val failed = AbilitiesState(error = AppError.Unexpected(IllegalStateException())).toUiModel()
        val fine = AbilitiesState(isLoading = false).toUiModel()

        assertTrue(failed.error != null)
        assertNull(fine.error)
    }
}
