package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMoveDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMoveLearnersUseCase
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

class MoveDetailExecutorTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val details = mapOf("flamethrower" to MoveFixtures.flamethrowerDetail)

    private fun store(
        repository: FakeMoveRepository,
        slug: String = "flamethrower",
    ) = MoveDetailStoreFactory(
        storeFactory = DefaultStoreFactory(),
        getMoveDetail = GetMoveDetailUseCase(repository),
        getMoveLearners = GetMoveLearnersUseCase(repository),
    ).create(slug)

    @Test
    fun store_readsTheMoveItWasBuiltForWithoutBeingAsked() =
        runTest {
            val store = store(FakeMoveRepository(details = details))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(MoveFixtures.flamethrowerDetail, state.move)
                assertNull(state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_turnsAnUnknownSlugIntoNotFoundRatherThanAnEmptyScreen() =
        runTest {
            // Only reachable when this build and the bundled dataset disagree, and a blank screen
            // would read as a rendering bug rather than as a missing row.
            val store = store(FakeMoveRepository(details = details), slug = "missingno")

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(AppError.Database.NotFound, state.error)
                assertNull(state.move)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_reportsAFailedRead() =
        runTest {
            val store = store(FakeMoveRepository(failure = IllegalStateException("no database")))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertTrue(state.error != null)
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

                store.accept(MoveDetailIntent.RetryClicked)
                while (awaitItem().isLoading) Unit

                assertEquals(2, repository.detailCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
