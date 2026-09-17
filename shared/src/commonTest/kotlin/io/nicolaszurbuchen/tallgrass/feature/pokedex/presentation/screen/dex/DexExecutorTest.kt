package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.fake.FakePokedexRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
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

class DexExecutorTest {
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

    private val entry =
        DexEntry(
            slug = "bulbasaur",
            dexNumber = 1,
            name = "Bulbasaur",
            formLabel = null,
            artworkUrl = "https://example.invalid/1.png",
            primaryType = PokemonType.GRASS,
            secondaryType = null,
        )

    private fun store(repository: FakePokedexRepository) =
        DexStoreFactory(
            storeFactory = DefaultStoreFactory(),
            getDexEntries = GetDexEntriesUseCase(repository),
        ).create()

    @Test
    fun store_loadsTheDexWithoutBeingAsked() =
        runTest {
            // The bootstrapper is what makes the grid populate on arrival rather than on a tap.
            val store = store(FakePokedexRepository(entries = listOf(entry)))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(listOf(entry), state.entries)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_reportsAFailureInsteadOfShowingAnEmptyDex() =
        runTest {
            val store = store(FakePokedexRepository(failure = IllegalStateException("no database")))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertTrue(state.error != null)
                assertTrue(state.entries.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun entryClicked_publishesWhatTheDetailHeroOpensWith() =
        runTest {
            // Not only the slug: the hero draws the artwork and the colour before it has read
            // anything, and the card that was tapped is where both are already known.
            val store = store(FakePokedexRepository(entries = listOf(entry)))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            store.labels.test {
                store.accept(DexIntent.EntryClicked("bulbasaur"))

                assertEquals(
                    DexLabel.NavigateToDetail(
                        slug = "bulbasaur",
                        artworkUrl = "https://example.invalid/1.png",
                        primaryTypeSlug = "grass",
                    ),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun entryClicked_saysNothingAboutACardTheDexDoesNotHave() =
        runTest {
            // Only reachable if a tap outlives the list it was made against. Publishing a label with
            // no artwork behind it would open a detail screen with an empty hero.
            val store = store(FakePokedexRepository(entries = listOf(entry)))

            store.labels.test {
                store.accept(DexIntent.EntryClicked("missingno"))

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun retryClicked_asksTheRepositoryAgain() =
        runTest {
            val repository = FakePokedexRepository(entries = listOf(entry))
            val store = store(repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(DexIntent.RetryClicked)
                state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(2, repository.callCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
