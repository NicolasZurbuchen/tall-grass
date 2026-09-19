package io.nicolaszurbuchen.tallgrass.design.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntranceTest {
    @Test
    fun entranceFraction_holdsAnItemAtZeroUntilItsTurn() {
        // The second item waits 55ms before it starts moving at all.
        assertEquals(0f, entranceFraction(viewportIndex = 1, elapsedMillis = 0))
        assertEquals(0f, entranceFraction(viewportIndex = 1, elapsedMillis = 54))
        assertTrue(entranceFraction(viewportIndex = 1, elapsedMillis = 56) > 0f)
    }

    @Test
    fun entranceFraction_runsToOneAndStopsThere() {
        assertEquals(1f, entranceFraction(viewportIndex = 0, elapsedMillis = AppDuration.MEDIUM))
        assertEquals(1f, entranceFraction(viewportIndex = 0, elapsedMillis = 10_000))
    }

    @Test
    fun entranceFraction_isAlreadyDoneForAnItemScrolledToLater() {
        // The property that makes a screen-level clock work at all: a card composed after the
        // entrance has finished reads 1 and draws with no animation rather than entering again.
        val afterTheClockStops = 10_000

        assertEquals(1f, entranceFraction(viewportIndex = 0, elapsedMillis = afterTheClockStops))
        assertEquals(1f, entranceFraction(viewportIndex = 7, elapsedMillis = afterTheClockStops))
    }

    @Test
    fun entranceFraction_staggersTheItemsAheadOfIt() {
        val elapsed = 200

        val first = entranceFraction(viewportIndex = 0, elapsedMillis = elapsed)
        val second = entranceFraction(viewportIndex = 1, elapsedMillis = elapsed)
        val third = entranceFraction(viewportIndex = 2, elapsedMillis = elapsed)

        assertTrue(first > second, "first $first should lead second $second")
        assertTrue(second > third, "second $second should lead third $third")
    }

    @Test
    fun entranceFraction_neverGoesNegativeOrPastOne() {
        val samples =
            listOf(-100, 0, 1, 200, 500, Int.MAX_VALUE / 2).flatMap { elapsed ->
                listOf(-5, 0, 3, 8, 2000).map { index -> entranceFraction(index, elapsed) }
            }

        assertTrue(samples.all { it in 0f..1f }, "Out of range: ${samples.filterNot { it in 0f..1f }}")
    }
}
