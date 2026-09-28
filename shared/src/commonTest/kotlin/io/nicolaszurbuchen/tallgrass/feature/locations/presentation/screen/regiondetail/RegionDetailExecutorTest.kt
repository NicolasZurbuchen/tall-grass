package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionDexUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionLocationsUseCase
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
import kotlin.test.assertNotNull

class RegionDetailExecutorTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val alolanVulpix =
        RegionDexEntry(
            slug = "vulpix-alola",
            cardSlug = "vulpix",
            number = 253,
            dexNumber = 37,
            name = "Vulpix",
            artworkUrl = "vulpix-alola.png",
            primaryType = PokemonType.ICE,
            secondaryType = null,
        )

    private fun store(
        repository: FakeLocationRepository,
        slug: String = "kanto",
    ) = RegionDetailStoreFactory(
        storeFactory = DefaultStoreFactory(),
        getRegionDetail = GetRegionDetailUseCase(repository),
        getRegionLocations = GetRegionLocationsUseCase(repository),
        getRegionDex = GetRegionDexUseCase(repository),
    ).create(slug)

    private fun repository() =
        FakeLocationRepository(
            regionDetails = mapOf("kanto" to LocationFixtures.kantoDetail),
            locations = mapOf("kanto" to listOf(LocationFixtures.route1Summary)),
            dexes = mapOf("kanto" to listOf(alolanVulpix)),
        )

    @Test
    fun store_readsAllThreeTabsWithoutBeingAsked() =
        runTest {
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(LocationFixtures.kantoDetail, state.region)
                assertEquals(1, state.locations.size)
                assertEquals(1, state.dex.size)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun aRegionWithNoRow_landsAsNotFoundRatherThanAnEmptyScreen() =
        runTest {
            val store = store(repository(), slug = "atlantis")

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                assertEquals(AppError.Database.NotFound, state.error)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun pokemonClicked_publishesTheFormAndTheCardItOpensThrough() =
        runTest {
            // A regional dex names forms that are not cards, so both slugs have to cross: the form
            // is what the hero draws, and the card is the door. See #5.
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            store.labels.test {
                store.accept(RegionDetailIntent.PokemonClicked("vulpix-alola"))

                val label = awaitItem() as RegionDetailLabel.NavigateToPokemon

                assertEquals("vulpix", label.cardSlug)
                assertEquals("vulpix-alola", label.formSlug)
                assertEquals("ice", label.primaryTypeSlug)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun pokemonClicked_forSomethingNotInTheDexPublishesNothing() =
        runTest {
            // The card is looked up rather than carried, so a slug that is not in this region has
            // nothing to hand forward and the screen stays where it is.
            val store = store(repository())

            store.stateFlow.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            store.labels.test {
                store.accept(RegionDetailIntent.PokemonClicked("missingno"))
                expectNoEvents()
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
