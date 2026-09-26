package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetEncounterConditionsUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetLocationDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetLocationEncountersUseCase
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
import kotlin.test.assertTrue

class LocationDetailExecutorTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun store(
        repository: FakeLocationRepository,
        slug: String = "kanto-route-1",
        versionSlug: String? = null,
    ) = LocationDetailStoreFactory(
        storeFactory = DefaultStoreFactory(),
        getLocationDetail = GetLocationDetailUseCase(repository),
        getEncounters = GetLocationEncountersUseCase(repository),
        getConditions = GetEncounterConditionsUseCase(repository),
    ).create(slug, versionSlug)

    private fun repository() =
        FakeLocationRepository(
            locationDetails = mapOf("kanto-route-1" to LocationFixtures.route1),
            encountersAt =
                mapOf(
                    ("kanto-route-1" to "heartgold") to
                        listOf(LocationFixtures.pidgeyByDay, LocationFixtures.rattataByNight),
                ),
            conditions = LocationFixtures.timeOfDay,
        )

    @Test
    fun store_readsThePlaceWithoutBeingAsked() =
        runTest {
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(LocationFixtures.route1, state.location)
                assertEquals(LocationFixtures.timeOfDay, state.conditions)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_doesNotReadAnyEncountersUntilAGameIsChosen() =
        runTest {
            // A busy route in a Generation VIII game is hundreds of rows across a dozen versions,
            // and all but one of them is behind a cell nobody tapped.
            val repository = repository()
            val store = store(repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(0, repository.encountersAtCallCount)
            store.dispose()
        }

    @Test
    fun choosingAGame_readsThatGamesRows() =
        runTest {
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(LocationDetailIntent.VersionSelected("heartgold"))
                state = awaitItem()
                while (state.isLoadingEncounters) state = awaitItem()

                assertEquals(2, state.encounters.size)
                assertEquals("walk", state.method)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun choosingAGreyCell_stillReadsAndComesBackEmpty() =
        runTest {
            // The two empty states are told apart by what the screen knows about the game, not by
            // refusing to look -- so a grey cell is selected like any other. See #21.
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(LocationDetailIntent.VersionSelected("red"))
                state = awaitItem()
                while (state.isLoadingEncounters) state = awaitItem()

                assertEquals("red", state.version)
                assertTrue(state.encounters.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun pokemonClicked_publishesTheFormAndTheCardItOpensThrough() =
        runTest {
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(LocationDetailIntent.VersionSelected("heartgold"))
                state = awaitItem()
                while (state.isLoadingEncounters) state = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            store.labels.test {
                store.accept(LocationDetailIntent.PokemonClicked("pidgey"))

                val label = awaitItem() as LocationDetailLabel.NavigateToPokemon

                assertEquals("pidgey", label.cardSlug)
                assertEquals("normal", label.primaryTypeSlug)
                assertEquals("flying", label.secondaryTypeSlug)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun pokemonClicked_forARowThatIsNotThereDoesNothing() =
        runTest {
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            store.labels.test {
                store.accept(LocationDetailIntent.PokemonClicked("missingno"))
                expectNoEvents()
            }
            store.dispose()
        }

    @Test
    fun aPlaceWithNoRow_landsAsNotFound() =
        runTest {
            val store = store(repository(), slug = "kanto-route-99")

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(AppError.Database.NotFound, state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun aFailedRead_landsAsAnError() =
        runTest {
            val store = store(FakeLocationRepository(failure = IllegalStateException("no database")))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertNotNull(state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
