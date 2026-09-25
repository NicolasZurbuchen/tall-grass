package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMovesForVariantUseCase
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.fake.FakePokedexRepository
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetPokemonDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.type.domain.fake.FakeTypeRepository
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy
import io.nicolaszurbuchen.tallgrass.core.type.domain.usecase.GetTypeMatchupsUseCase
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
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

class DetailExecutorTest {
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

    private val chart =
        FakeTypeRepository(
            mapOf(
                PokemonType.FIRE to listOf(TypeEfficacy(PokemonType.ROCK, 200)),
                PokemonType.FLYING to listOf(TypeEfficacy(PokemonType.ROCK, 200)),
                PokemonType.DRAGON to listOf(TypeEfficacy(PokemonType.FAIRY, 200)),
            ),
        )

    private fun store(
        slug: String = "charizard",
        repository: FakePokedexRepository = FakePokedexRepository(details = mapOf("charizard" to charizardDetail)),
    ) = DetailStoreFactory(
        storeFactory = DefaultStoreFactory(),
        getDexEntries = GetDexEntriesUseCase(repository),
        getPokemonDetail = GetPokemonDetailUseCase(repository),
        getTypeMatchups = GetTypeMatchupsUseCase(chart),
        getMovesForVariant = GetMovesForVariantUseCase(FakeMoveRepository()),
    ).create(slug, DexQuery.All, formSlug = null)

    @Test
    fun store_readsTheDetailWithoutBeingAsked() =
        runTest {
            val store = store()

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(charizardDetail, state.details["charizard"])
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_readsTheMatchupsOfEveryFormRatherThanOnlyTheOneOnScreen() =
        runTest {
            // Switching form must not wait on a query, which is the whole reason the Stats tab can
            // move between Arceus's eighteen without a spinner between them.
            val store = store()

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(setOf("charizard", "charizard-mega-x"), state.matchups.keys)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_computesEachFormsMatchupsFromItsOwnTypes() =
        runTest {
            // Mega Charizard X is Fire/Dragon where Charizard is Fire/Flying, so the two differ by
            // more than their stats. Reading types off the species would make these identical.
            val store = store()

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                val ordinary = state.matchups.getValue("charizard").associate { it.attackingType to it.factorPercent }
                val mega = state.matchups.getValue("charizard-mega-x").associate { it.attackingType to it.factorPercent }

                assertEquals(400, ordinary[PokemonType.ROCK])
                assertEquals(200, mega[PokemonType.ROCK])
                assertEquals(200, mega[PokemonType.FAIRY])
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_reportsASlugTheDatasetDoesNotCarryAsMissingRatherThanBroken() =
        runTest {
            val store = store(slug = "missingno")

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(AppError.Database.NotFound, state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun store_reportsAFailedReadInsteadOfAnEmptyScreen() =
        runTest {
            val store = store(repository = FakePokedexRepository(failure = IllegalStateException("no database")))

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertTrue(state.error is AppError.Unexpected)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun backClicked_publishesTheOnlyLabelThisScreenHas() =
        runTest {
            val store = store()

            store.labels.test {
                store.accept(DetailIntent.BackClicked)

                assertEquals(DetailLabel.NavigateBack, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun retryClicked_asksTheRepositoryAgain() =
        runTest {
            val repository = FakePokedexRepository(details = mapOf("charizard" to charizardDetail))
            val store = store(repository = repository)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(DetailIntent.RetryClicked)
                state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(2, repository.detailCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
