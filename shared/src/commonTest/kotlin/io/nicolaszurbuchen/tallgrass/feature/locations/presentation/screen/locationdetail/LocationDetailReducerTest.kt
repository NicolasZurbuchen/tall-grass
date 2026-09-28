package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationDetailReducerTest {
    private val reduce = LocationDetailStoreFactory.ReducerImpl

    @Test
    fun loadStarted_clearsAPreviousError() =
        with(reduce) {
            val state =
                LocationDetailState(error = AppError.Database.NotFound)
                    .reduce(LocationDetailMessage.LoadStarted)

            assertTrue(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun locationLoaded_carriesTheConditionsAlongWithThePlace() =
        with(reduce) {
            // The condition values are read once with the location rather than per route change:
            // they are the same 361 rows whichever game is chosen.
            val state =
                LocationDetailState(isLoading = true).reduce(
                    LocationDetailMessage.LocationLoaded(LocationFixtures.route1, LocationFixtures.timeOfDay),
                )

            assertEquals(LocationFixtures.route1, state.location)
            assertEquals(LocationFixtures.timeOfDay, state.conditions)
            assertEquals(false, state.isLoading)
        }

    @Test
    fun choosingAGame_clearsTheMethodAndThePins() =
        with(reduce) {
            // Both belong to the table about to be replaced. Route 1 varies on three axes in
            // HeartGold and on none in Red, so a pin left over would silently filter the second.
            val before =
                LocationDetailState(
                    version = "heartgold",
                    method = "walk",
                    pinned = mapOf("time" to "time-night"),
                    encounters = listOf(LocationFixtures.pidgeyByDay),
                )

            val after = before.reduce(LocationDetailMessage.VersionSelected("red"))

            assertEquals("red", after.version)
            assertNull(after.method)
            assertTrue(after.pinned.isEmpty())
            assertTrue(after.encounters.isEmpty())
            assertTrue(after.isLoadingEncounters)
        }

    @Test
    fun clearingTheGame_putsTheGridBack() =
        with(reduce) {
            val state =
                LocationDetailState(version = "heartgold", encounters = listOf(LocationFixtures.pidgeyByDay))
                    .reduce(LocationDetailMessage.VersionCleared)

            assertNull(state.version)
            assertTrue(state.encounters.isEmpty())
        }

    @Test
    fun encountersLoaded_selectTheFirstMethodPresent() =
        with(reduce) {
            // A route has no fixed set of methods, so there is nothing to default to before the
            // rows are in.
            val state =
                LocationDetailState(isLoadingEncounters = true)
                    .reduce(LocationDetailMessage.EncountersLoaded(listOf(LocationFixtures.pidgeyByDay)))

            assertEquals("walk", state.method)
            assertEquals(false, state.isLoadingEncounters)
        }

    @Test
    fun anEmptyRead_leavesNoMethodSelected() =
        with(reduce) {
            val state =
                LocationDetailState(isLoadingEncounters = true)
                    .reduce(LocationDetailMessage.EncountersLoaded(emptyList()))

            assertNull(state.method)
        }

    @Test
    fun changingMethod_clearsThePins() =
        with(reduce) {
            // Each method is its own table with its own axes, and a time pinned on the walking
            // table means nothing to the fishing one.
            val state =
                LocationDetailState(method = "walk", pinned = mapOf("time" to "time-night"))
                    .reduce(LocationDetailMessage.MethodSelected("old-rod"))

            assertEquals("old-rod", state.method)
            assertTrue(state.pinned.isEmpty())
        }

    @Test
    fun pinningAnAxis_keepsTheOthersAlone() =
        with(reduce) {
            val state =
                LocationDetailState(pinned = mapOf("swarm" to "swarm-no"))
                    .reduce(LocationDetailMessage.ConditionSelected("time", "time-night"))

            assertEquals(mapOf("swarm" to "swarm-no", "time" to "time-night"), state.pinned)
        }

    @Test
    fun choosingAnyOnAnAxis_unpinsItRatherThanStoringAValue() =
        with(reduce) {
            // "Any" is the absence of a pin, not a value of its own: that is what lets it mean the
            // best case across every state.
            val state =
                LocationDetailState(pinned = mapOf("time" to "time-night", "swarm" to "swarm-no"))
                    .reduce(LocationDetailMessage.ConditionSelected("time", null))

            assertEquals(mapOf("swarm" to "swarm-no"), state.pinned)
        }

    @Test
    fun loadFailed_stopsBothLoadingFlags() =
        with(reduce) {
            val state =
                LocationDetailState(isLoading = true, isLoadingEncounters = true)
                    .reduce(LocationDetailMessage.LoadFailed(AppError.Database.NotFound))

            assertEquals(false, state.isLoading)
            assertEquals(false, state.isLoadingEncounters)
            assertEquals(AppError.Database.NotFound, state.error)
        }
}
