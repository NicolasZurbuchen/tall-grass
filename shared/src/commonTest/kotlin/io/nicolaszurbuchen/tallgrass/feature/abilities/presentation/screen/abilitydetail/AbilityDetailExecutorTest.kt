package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.FakeAbilityRepository
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilityDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilityHoldersUseCase
import io.nicolaszurbuchen.tallgrass.core.error.AppError
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

class AbilityDetailExecutorTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val details = mapOf("levitate" to AbilityFixtures.levitateDetail)
    private val holders = mapOf("levitate" to listOf(AbilityFixtures.gastly, AbilityFixtures.vibrava))

    private fun store(
        repository: FakeAbilityRepository,
        slug: String = "levitate",
    ) = AbilityDetailStoreFactory(
        storeFactory = DefaultStoreFactory(),
        getAbilityDetail = GetAbilityDetailUseCase(repository),
        getAbilityHolders = GetAbilityHoldersUseCase(repository),
    ).create(slug)

    @Test
    fun store_readsTheAbilityItWasBuiltForWithoutBeingAsked() =
        runTest {
            val store = store(FakeAbilityRepository(details = details, holders = holders))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(AbilityFixtures.levitateDetail, state.ability)
                assertNull(state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_readsTheHoldersInTheSameLoadRatherThanWhenTheirTabOpens() =
        runTest {
            // The Details tab counts them, so the first tab cannot draw itself without them.
            val repository = FakeAbilityRepository(details = details, holders = holders)
            val store = store(repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(2, state.holders.size)
                assertEquals(1, repository.holdersCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun aSlugWithNoRowIsNotFoundRatherThanAnEmptyScreen() =
        runTest {
            // A missing slug means this build and the bundled dataset disagree, which is an error
            // rather than an ability with nothing in it.
            val store = store(FakeAbilityRepository(details = details), slug = "missingno")

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(AppError.Database.NotFound, state.error)
                assertNull(state.ability)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun anAbilityNothingHasStillLoads() =
        runTest {
            // An empty holder list is an answer rather than a failure, unlike a missing slug.
            val store = store(FakeAbilityRepository(details = mapOf("stench" to AbilityFixtures.stenchDetail)), "stench")

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(AbilityFixtures.stenchDetail, state.ability)
                assertTrue(state.holders.isEmpty())
                assertNull(state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun holderClicked_publishesTheCardAndTheFormSeparately() =
        runTest {
            // The card is what the Pokemon detail's carousel walks and the form is what it opens on,
            // which are the same string here and are not for any variant.
            val store = store(FakeAbilityRepository(details = details, holders = holders))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.labels.test {
                    store.accept(AbilityDetailIntent.HolderClicked("vibrava"))

                    val label = awaitItem() as AbilityDetailLabel.NavigateToPokemon
                    assertEquals("vibrava", label.cardSlug)
                    assertEquals("vibrava", label.formSlug)
                    assertEquals("ground", label.primaryTypeSlug)
                    assertEquals("dragon", label.secondaryTypeSlug)
                    cancelAndIgnoreRemainingEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun holderClicked_publishesNothingForARowTheStoreDoesNotHold() =
        runTest {
            // The hero's contents are read back out of state, so a slug that is not there has no
            // artwork to open with and the tap is dropped rather than half-answered.
            val store = store(FakeAbilityRepository(details = details, holders = holders))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.labels.test {
                    store.accept(AbilityDetailIntent.HolderClicked("missingno"))

                    expectNoEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun retryClicked_readsBothHalvesAgain() =
        runTest {
            val repository = FakeAbilityRepository(details = details, holders = holders)
            val store = store(repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(AbilityDetailIntent.RetryClicked)
                state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(2, repository.detailCallCount)
                assertEquals(2, repository.holdersCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
