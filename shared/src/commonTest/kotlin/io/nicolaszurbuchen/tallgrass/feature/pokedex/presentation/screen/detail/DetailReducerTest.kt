package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DetailReducerTest {
    private val reducer = DetailStoreFactory.ReducerImpl

    private fun reduce(
        state: DetailState,
        message: DetailMessage,
    ) = with(reducer) { state.reduce(message) }

    private val initial = DetailState(entryVariantSlug = "charizard", query = DexQuery.All)

    private fun loaded(entrySlug: String = "charizard") =
        DetailMessage.DetailLoaded(entrySlug = entrySlug, detail = charizardDetail, matchups = emptyMap())

    @Test
    fun detailLoaded_opensOnTheFormThatWasTapped() {
        val state = reduce(DetailState(entryVariantSlug = "charizard-mega-x", query = DexQuery.All), loaded("charizard-mega-x"))

        assertEquals("charizard-mega-x", state.activeVariantSlug)
        assertFalse(state.isLoading)
    }

    @Test
    fun detailLoaded_fallsBackToTheFirstFormWhenTheTappedOneIsGone() {
        // A saved back stack can name a slug a newer dataset no longer carries. The species is still
        // worth a screen, so the switcher opens on its first form rather than on nothing.
        val state = reduce(DetailState(entryVariantSlug = "charizard-mega-z", query = DexQuery.All), loaded("charizard-mega-z"))

        assertEquals("charizard", state.activeVariantSlug)
    }

    @Test
    fun formSwitched_movesTheActiveFormAndLeavesTheEntryOneAlone() {
        // The two together are what decide whether the hero still owns the shared element it arrived
        // with, so the entry slug has to survive the switch.
        val state = reduce(reduce(initial, loaded()), DetailMessage.FormSwitched("charizard-mega-x"))

        assertEquals("charizard-mega-x", state.activeVariantSlug)
        assertEquals("charizard", state.entryVariantSlug)
    }

    @Test
    fun formSwitched_doesNotTouchTheDetailItSwitchesWithin() {
        // Every form was read at once, so switching is a choice within what is already in hand.
        val state = reduce(reduce(initial, loaded()), DetailMessage.FormSwitched("charizard-mega-x"))

        assertEquals(charizardDetail, state.details["charizard"])
        assertFalse(state.isLoading)
    }

    @Test
    fun tabSwitched_changesOnlyTheTab() {
        val state = reduce(reduce(initial, loaded()), DetailMessage.TabSwitched(DetailState.Tab.STATS))

        assertEquals(DetailState.Tab.STATS, state.tab)
        assertEquals("charizard", state.activeVariantSlug)
    }

    @Test
    fun loadFailed_stopsLoadingAndKeepsTheError() {
        val state = reduce(initial, DetailMessage.LoadFailed(AppError.Database.NotFound))

        assertFalse(state.isLoading)
        assertEquals(AppError.Database.NotFound, state.error)
    }

    @Test
    fun loadStarted_clearsTheErrorSoARetryDoesNotShowTheOldOne() {
        val failed = reduce(initial, DetailMessage.LoadFailed(AppError.Database.NotFound))

        val state = reduce(failed, DetailMessage.LoadStarted)

        assertTrue(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun switchingForm_clearsTheChosenGameAndItsPlaces() {
        // Alolan Vulpix is not found where Vulpix is, so a cell left selected would show one form's
        // places under another form's name.
        val browsing =
            initial.copy(
                locationVersion = "heartgold",
                places = listOf(LocationFixtures.pidgeyOnRoute1),
            )

        val state = reduce(browsing, DetailMessage.FormSwitched("charizard-mega-x"))

        assertNull(state.locationVersion)
        assertTrue(state.places.isEmpty())
    }

    @Test
    fun availabilityIsKeptPerFormRatherThanForTheOneOnScreen() {
        // A reader comparing two forms switches back and forth, and the second look should not read
        // again -- the same call the moves cache makes.
        val first =
            reduce(initial, DetailMessage.AvailabilityLoaded("charizard", LocationFixtures.pidgeyAvailability))
        val second =
            reduce(first, DetailMessage.AvailabilityLoaded("charizard-mega-x", LocationFixtures.pidgeyAvailability))

        assertEquals(setOf("charizard", "charizard-mega-x"), second.availability.keys)
    }

    @Test
    fun choosingAGame_dropsThePreviousPlacesWhileTheNextOnesLoad() {
        // Otherwise the rows for the old game sit under the new game's name for as long as the read
        // takes, which is the one moment they are certainly wrong.
        val browsing = initial.copy(locationVersion = "red", places = listOf(LocationFixtures.pidgeyOnRoute1))

        val state = reduce(browsing, DetailMessage.LocationVersionSelected("heartgold"))

        assertEquals("heartgold", state.locationVersion)
        assertTrue(state.isLoadingPlaces)
        assertTrue(state.places.isEmpty())
    }

    @Test
    fun clearingTheGame_putsTheGridBack() {
        val browsing = initial.copy(locationVersion = "heartgold", places = listOf(LocationFixtures.pidgeyOnRoute1))

        val state = reduce(browsing, DetailMessage.LocationVersionCleared)

        assertNull(state.locationVersion)
        assertTrue(state.places.isEmpty())
    }
}
