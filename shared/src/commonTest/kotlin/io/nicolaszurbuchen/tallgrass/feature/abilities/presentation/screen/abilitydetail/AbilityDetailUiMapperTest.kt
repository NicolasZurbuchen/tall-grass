package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityDetailTabUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilityDetailUiMapperTest {
    @Test
    fun anUnreadAbilityLeavesTheContentNullRatherThanEmpty() {
        // Nothing is drawn before the read lands: the transition into this screen is a push and
        // hands nothing forward, so there is no half-built ability to render.
        val ui = AbilityDetailState(isLoading = true).toUiModel()

        assertNull(ui.ability)
        assertTrue(ui.holders.isEmpty())
    }

    @Test
    fun theHoldersReachTheGridTheyAreListedIn() {
        val state =
            AbilityDetailState(
                isLoading = false,
                ability = AbilityFixtures.levitateDetail,
                holders = listOf(AbilityFixtures.gastly, AbilityFixtures.vibrava),
            )

        val ui = state.toUiModel()

        assertEquals(2, ui.holders.size)
        assertEquals("Levitate", ui.ability?.name)
    }

    @Test
    fun theTabCrossesFromTheStoresVocabularyToTheScreens() {
        val holders = AbilityDetailState(tab = AbilityDetailState.Tab.HOLDERS).toUiModel()
        val details = AbilityDetailState(tab = AbilityDetailState.Tab.DETAILS).toUiModel()

        assertEquals(AbilityDetailTabUiModel.HOLDERS, holders.tab)
        assertEquals(AbilityDetailTabUiModel.DETAILS, details.tab)
    }

    @Test
    fun anErrorIsCarriedAcrossAndAnAbsentOneStaysNull() {
        val failed = AbilityDetailState(error = AppError.Database.NotFound).toUiModel()
        val fine = AbilityDetailState(isLoading = false).toUiModel()

        assertTrue(failed.error != null)
        assertNull(fine.error)
    }
}
