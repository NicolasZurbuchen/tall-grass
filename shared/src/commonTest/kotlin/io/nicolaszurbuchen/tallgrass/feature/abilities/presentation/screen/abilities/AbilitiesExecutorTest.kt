package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.FakeAbilityRepository
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilitiesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilitiesExecutorTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun store(repository: FakeAbilityRepository) =
        AbilitiesStoreFactory(
            storeFactory = DefaultStoreFactory(),
            getAbilities = GetAbilitiesUseCase(repository),
        ).create()

    @Test
    fun store_readsTheAbilitiesWithoutBeingAsked() =
        runTest {
            val abilities = listOf(AbilityFixtures.adaptability, AbilityFixtures.levitate)
            val store = store(FakeAbilityRepository(abilities = abilities))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(abilities, state.abilities)
                assertNull(state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun abilityClicked_publishesTheSlugItWasGiven() =
        runTest {
            val store = store(FakeAbilityRepository(abilities = listOf(AbilityFixtures.levitate)))

            store.labels.test {
                store.accept(AbilitiesIntent.AbilityClicked("levitate"))

                assertEquals(AbilitiesLabel.NavigateToDetail("levitate"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun aFailedReadBecomesAnErrorRatherThanAnEmptyList() =
        runTest {
            val store = store(FakeAbilityRepository(failure = IllegalStateException("no database")))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertTrue(state.error != null)
                assertTrue(state.abilities.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun retryClicked_readsAgain() =
        runTest {
            val repository = FakeAbilityRepository(abilities = listOf(AbilityFixtures.levitate))
            val store = store(repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(AbilitiesIntent.RetryClicked)
                state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(2, repository.abilitiesCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
