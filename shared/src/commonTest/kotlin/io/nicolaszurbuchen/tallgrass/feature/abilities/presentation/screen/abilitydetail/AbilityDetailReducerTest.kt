package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilityDetailReducerTest {
    private val reduce = AbilityDetailStoreFactory.ReducerImpl

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            // Otherwise a retry would show the skeleton and the old error banner at once.
            val state =
                AbilityDetailState(error = AppError.Database.NotFound).reduce(AbilityDetailMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun abilityLoaded_stopsLoadingAndHoldsBothHalvesOfTheRead() =
        with(reduce) {
            val holders = listOf(AbilityFixtures.gastly, AbilityFixtures.vibrava)

            val state =
                AbilityDetailState(isLoading = true)
                    .reduce(AbilityDetailMessage.AbilityLoaded(AbilityFixtures.levitateDetail, holders))

            assertEquals(AbilityFixtures.levitateDetail, state.ability)
            assertEquals(holders, state.holders)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun tabChanged_survivesAReload() =
        with(reduce) {
            // A retry from the Known by tab should come back to it rather than to the tab the screen
            // happened to open on, so LoadStarted deliberately leaves this alone.
            val onHolders = AbilityDetailState(tab = AbilityDetailState.Tab.HOLDERS)

            val state = onHolders.reduce(AbilityDetailMessage.LoadStarted)

            assertEquals(AbilityDetailState.Tab.HOLDERS, state.tab)
        }

    @Test
    fun tabChanged_movesTheTabAndTouchesNothingElse() =
        with(reduce) {
            val loaded =
                AbilityDetailState(
                    isLoading = false,
                    ability = AbilityFixtures.levitateDetail,
                    holders = listOf(AbilityFixtures.gastly),
                )

            val state = loaded.reduce(AbilityDetailMessage.TabChanged(AbilityDetailState.Tab.HOLDERS))

            assertEquals(AbilityDetailState.Tab.HOLDERS, state.tab)
            assertEquals(AbilityFixtures.levitateDetail, state.ability)
            assertEquals(listOf(AbilityFixtures.gastly), state.holders)
        }

    @Test
    fun loadFailed_stopsLoadingAndKeepsWhatWasAlreadyShown() =
        with(reduce) {
            val loaded = AbilityDetailState(isLoading = false, ability = AbilityFixtures.levitateDetail)

            val state = loaded.reduce(AbilityDetailMessage.LoadFailed(AppError.Unexpected(IllegalStateException())))

            assertEquals(AbilityFixtures.levitateDetail, state.ability)
            assertEquals(false, state.isLoading)
            assertTrue(state.error != null)
        }
}
