package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilitiesReducerTest {
    private val reduce = AbilitiesStoreFactory.ReducerImpl

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            // Otherwise a retry would show the skeleton and the old error banner at once.
            val state =
                AbilitiesState(error = AppError.Unexpected(IllegalStateException()))
                    .reduce(AbilitiesMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun abilitiesLoaded_stopsLoadingAndHoldsTheAbilities() =
        with(reduce) {
            val abilities = listOf(AbilityFixtures.adaptability, AbilityFixtures.levitate)

            val state = AbilitiesState(isLoading = true).reduce(AbilitiesMessage.AbilitiesLoaded(abilities))

            assertEquals(abilities, state.abilities)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun abilitiesLoaded_handsBackTheSameListInstanceItWasGiven() =
        with(reduce) {
            // Identity is what AbilitiesViewModel keys its card cache on, so a reducer that copied
            // the list would silently remap all 314 cards on every state.
            val abilities = listOf(AbilityFixtures.levitate)

            val state = AbilitiesState().reduce(AbilitiesMessage.AbilitiesLoaded(abilities))

            assertTrue(state.abilities === abilities)
        }

    @Test
    fun loadFailed_stopsLoadingAndKeepsWhatWasAlreadyShown() =
        with(reduce) {
            val loaded = AbilitiesState(abilities = listOf(AbilityFixtures.levitate), isLoading = false)

            val state = loaded.reduce(AbilitiesMessage.LoadFailed(AppError.Unexpected(IllegalStateException())))

            assertEquals(listOf(AbilityFixtures.levitate), state.abilities)
            assertEquals(false, state.isLoading)
            assertTrue(state.error != null)
        }
}
