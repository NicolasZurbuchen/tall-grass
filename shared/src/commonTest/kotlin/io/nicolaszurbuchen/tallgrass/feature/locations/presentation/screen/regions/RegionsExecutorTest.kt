package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RegionsExecutorTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun store(repository: FakeLocationRepository) =
        RegionsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            getRegions = GetRegionsUseCase(repository),
        ).create()

    @Test
    fun store_readsTheRegionsWithoutBeingAsked() =
        runTest {
            val regions = listOf(LocationFixtures.kanto, LocationFixtures.orre)
            val store = store(FakeLocationRepository(regions = regions))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(regions, state.regions)
                assertNull(state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun regionClicked_publishesTheSlugItWasGiven() =
        runTest {
            val store = store(FakeLocationRepository(regions = listOf(LocationFixtures.kanto)))

            store.labels.test {
                store.accept(RegionsIntent.RegionClicked("kanto"))

                assertEquals(RegionsLabel.NavigateToDetail("kanto"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun aFailedRead_landsAsAnErrorRatherThanAnEmptyList() =
        runTest {
            // An empty list and a broken read look the same on screen otherwise, and one of them
            // has a retry.
            val store = store(FakeLocationRepository(failure = IllegalStateException("no database")))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertNotNull(state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun retry_readsAgain() =
        runTest {
            val repository = FakeLocationRepository(regions = listOf(LocationFixtures.kanto))
            val store = store(repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(RegionsIntent.RetryClicked)
                state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(2, repository.regionsCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
