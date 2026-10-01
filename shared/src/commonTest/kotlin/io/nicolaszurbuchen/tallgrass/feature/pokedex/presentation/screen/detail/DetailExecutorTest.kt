package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.FakeAbilityRepository
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilitiesForVariantUseCase
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetVariantAvailabilityUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetVariantEncountersUseCase
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MaxMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMaxMovesForVariantUseCase
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
        moves: FakeMoveRepository = FakeMoveRepository(),
        abilities: FakeAbilityRepository = FakeAbilityRepository(),
        locations: FakeLocationRepository = FakeLocationRepository(),
    ) = DetailStoreFactory(
        storeFactory = DefaultStoreFactory(),
        getDexEntries = GetDexEntriesUseCase(repository),
        getPokemonDetail = GetPokemonDetailUseCase(repository),
        getTypeMatchups = GetTypeMatchupsUseCase(chart),
        getMovesForVariant = GetMovesForVariantUseCase(moves),
        getMaxMovesForVariant = GetMaxMovesForVariantUseCase(moves),
        getAbilitiesForVariant = GetAbilitiesForVariantUseCase(abilities),
        getVariantAvailability = GetVariantAvailabilityUseCase(locations),
        getVariantEncounters = GetVariantEncountersUseCase(locations),
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

    @Test
    fun tabSelected_fillsBothHalvesOfTheMovesTab() =
        runTest {
            // Abilities and moves are one read, because they are one tab. A reader who never opens
            // it pays for neither.
            val abilities =
                FakeAbilityRepository(
                    variantAbilities = mapOf("charizard" to listOf(AbilityFixtures.blaze, AbilityFixtures.solarPower)),
                )
            val store = store(abilities = abilities)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(0, abilities.abilitiesForCallCount)

                store.accept(DetailIntent.TabSelected(DetailState.Tab.MOVES))
                while (state.abilities["charizard"] == null) state = awaitItem()

                assertEquals(listOf(AbilityFixtures.blaze, AbilityFixtures.solarPower), state.abilities["charizard"])
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun tabSelected_doesNotReadAgainForAFormAlreadyRead() =
        runTest {
            // Held per variant once read, on the same grounds as the moves beside them: neither can
            // change under a running app, both are baked into the binary.
            val abilities =
                FakeAbilityRepository(variantAbilities = mapOf("charizard" to listOf(AbilityFixtures.blaze)))
            val store = store(abilities = abilities)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(DetailIntent.TabSelected(DetailState.Tab.MOVES))
                while (state.abilities["charizard"] == null) state = awaitItem()

                store.accept(DetailIntent.TabSelected(DetailState.Tab.ABOUT))
                store.accept(DetailIntent.TabSelected(DetailState.Tab.MOVES))

                assertEquals(1, abilities.abilitiesForCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun entrySelected_readsTheOpenTabForTheCardItLandsOn() =
        runTest {
            // The lazy reads were wired to the tab *changing*, so a reader already on Location who
            // swiped kept a tab that had never read for the card now in front of them -- and absence
            // is how that tab says "not read yet", so it sat on its skeleton until they left it.
            val locations = FakeLocationRepository()
            val repository =
                FakePokedexRepository(
                    details = mapOf("charizard" to charizardDetail, "bulbasaur" to bulbasaurDetail),
                )
            val store = store(repository = repository, locations = locations)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(DetailIntent.TabSelected(DetailState.Tab.LOCATION))
                while (state.availability["charizard"] == null) state = awaitItem()

                store.accept(DetailIntent.EntrySelected("bulbasaur"))
                while (state.availability["bulbasaur"] == null) state = awaitItem()

                assertEquals(2, locations.availabilityCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun entrySelected_readsTheMovesTabForTheCardItLandsOn() =
        runTest {
            // The same bug from the other tab, where it read as a Pokemon that knows nothing.
            val moves = FakeMoveRepository()
            val repository =
                FakePokedexRepository(
                    details = mapOf("charizard" to charizardDetail, "bulbasaur" to bulbasaurDetail),
                )
            val store = store(repository = repository, moves = moves)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(DetailIntent.TabSelected(DetailState.Tab.MOVES))
                while (state.moves["charizard"] == null) state = awaitItem()

                store.accept(DetailIntent.EntrySelected("bulbasaur"))
                while (state.moves["bulbasaur"] == null) state = awaitItem()

                assertEquals(2, moves.movesForCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun formSelected_onAGigantamaxForm_readsTheMaxMovesItsOwnMovesBecome() =
        runTest {
            // The wiring the conversion hangs off: the executor has to notice that the form now on
            // screen has a G-Max Move and ask for the converted list. Miss it and the tab quietly
            // falls back to the moves the Pokemon learnt, which is not what it uses.
            val signature =
                MoveFixtures.charizardFlamethrower.copy(
                    slug = "g-max-wildfire",
                    method = LearnMethod.GIGANTAMAX,
                    level = null,
                )
            val moves =
                FakeMoveRepository(
                    variantMoves = mapOf("charizard-gmax" to listOf(MoveFixtures.charizardFlamethrower, signature)),
                    maxMoves = listOf(MaxMove("g-max-wildfire", "G-Max Wildfire", PokemonType.FIRE, power = null)),
                )
            val repository = FakePokedexRepository(details = mapOf("charizard" to charizardGmaxDetail))
            val store = store(repository = repository, moves = moves)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(DetailIntent.TabSelected(DetailState.Tab.MOVES))
                while (state.moves["charizard"] == null) state = awaitItem()

                // The base form has none, which is the other half of the claim.
                assertEquals(null, state.maxMoves["charizard"])

                store.accept(DetailIntent.FormSelected("charizard-gmax"))
                while (state.maxMoves["charizard-gmax"] == null) state = awaitItem()

                assertEquals(listOf("g-max-wildfire"), state.maxMoves["charizard-gmax"]?.map { it.slug })
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun formSelected_readsWhicheverTabIsOpenRatherThanAlwaysTheMoves() =
        runTest {
            // A form is not found where its base form is, so the Location tab has its own read per
            // variant -- and switching form used to fire the Moves read whatever tab was in front of
            // the reader, which left this one on nothing.
            val locations = FakeLocationRepository()
            val store = store(locations = locations)

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                store.accept(DetailIntent.TabSelected(DetailState.Tab.LOCATION))
                while (state.availability["charizard"] == null) state = awaitItem()

                store.accept(DetailIntent.FormSelected("charizard-mega-x"))
                while (state.availability["charizard-mega-x"] == null) state = awaitItem()

                assertEquals(2, locations.availabilityCallCount)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun abilityClicked_publishesTheSlugItWasGiven() =
        runTest {
            val store = store()

            store.labels.test {
                store.accept(DetailIntent.AbilityClicked("blaze"))

                assertEquals(DetailLabel.NavigateToAbility("blaze"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
}
