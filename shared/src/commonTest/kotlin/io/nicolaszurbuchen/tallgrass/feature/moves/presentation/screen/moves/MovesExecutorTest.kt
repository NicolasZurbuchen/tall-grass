package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMovesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovesExecutorTest {
    // MVIKotlin dispatches store work on the main thread, which a host test has to provide before
    // the first store is built rather than lazily on first use.
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun store(repository: FakeMoveRepository) =
        MovesStoreFactory(
            storeFactory = DefaultStoreFactory(),
            getMoves = GetMovesUseCase(repository),
        ).create()

    @Test
    fun store_loadsTheMovesWithoutBeingAsked() =
        runTest {
            // The bootstrapper is what makes the list populate on arrival rather than on a tap.
            val store = store(FakeMoveRepository(moves = listOf(MoveFixtures.flamethrower)))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(listOf(MoveFixtures.flamethrower), state.moves)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_reportsAFailureInsteadOfShowingAnEmptyList() =
        runTest {
            val store = store(FakeMoveRepository(failure = IllegalStateException("no database")))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertTrue(state.error != null)
                assertTrue(state.moves.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun moveClicked_publishesTheSlugAndNothingElse() =
        runTest {
            // Unlike the dex, which hands its hero forward: a move has no artwork to render
            // synchronously, so the detail is a push that reads what it needs. See #11.
            val store = store(FakeMoveRepository(moves = listOf(MoveFixtures.flamethrower)))

            store.labels.test {
                store.accept(MovesIntent.MoveClicked("flamethrower"))

                assertEquals(MovesLabel.NavigateToDetail("flamethrower"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun retryClicked_readsAgain() =
        runTest {
            val repository = FakeMoveRepository(failure = IllegalStateException("no database"))
            val store = store(repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(MovesIntent.RetryClicked)
                while (awaitItem().isLoading) Unit

                assertEquals(2, repository.movesCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
