package io.nicolaszurbuchen.tallgrass.infra.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImagePrefetchProgressTest {
    private fun progress(
        fetched: Int = 0,
        alreadyCached: Int = 0,
        failed: Int = 0,
        total: Int = 100,
        stoppedForSpace: Boolean = false,
    ) = ImagePrefetchProgress(fetched, alreadyCached, failed, total, stoppedForSpace)

    @Test
    fun handled_countsEveryUrlTheRunIsDoneWith() {
        // Including the ones it failed and the ones it skipped. A bar that only counted downloads
        // would never reach the end on a resumed run.
        assertEquals(30, progress(fetched = 10, alreadyCached = 15, failed = 5).handled)
    }

    @Test
    fun fraction_measuresAgainstTheWholeCorpusNotWhatIsLeft() {
        // A resumed run starts where the last one stopped instead of restarting the bar at zero.
        assertEquals(0.9f, progress(alreadyCached = 90).fraction)
    }

    @Test
    fun fraction_isOneWhenThereIsNothingToFetch() {
        // A build with no disk cache reports a total of zero rather than dividing by it.
        assertEquals(1f, progress(total = 0).fraction)
    }

    @Test
    fun fraction_neverLeavesItsRange() {
        assertEquals(1f, progress(fetched = 200, total = 100).fraction)
        assertEquals(0f, progress(total = 100).fraction)
    }

    @Test
    fun isComplete_isTrueWhenTheRunGaveUpForSpace() {
        // It stopped, so nothing more is coming -- which is what "complete" has to mean here, or a
        // caller waiting for the end waits forever.
        assertTrue(progress(fetched = 12, stoppedForSpace = true).isComplete)
        assertFalse(progress(fetched = 12).isComplete)
    }

    @Test
    fun isComplete_isTrueWhenEveryUrlHasBeenAccountedFor() {
        assertTrue(progress(fetched = 60, alreadyCached = 30, failed = 10).isComplete)
    }

    @Test
    fun reportingSlot_lettsARunOverTheWholeDexReportTwentyFiveTimes() {
        // The regression this exists for: emitting once per image put 1,082 state changes through a
        // mapper that rebuilds every card, 236ms of main thread measured, all of it arriving in one
        // burst at the moment the grid appeared.
        val dex = 1082
        val emissions = (1..dex).map { handled -> progress(fetched = handled, total = dex).reportingSlot }.distinct()

        assertTrue(emissions.size <= 26, "A full run would emit  times")
    }

    @Test
    fun reportingSlot_stillMovesForAShortRun() {
        // A species with a handful of forms must not collapse to a single emission at the end.
        val emissions = (1..10).map { handled -> progress(fetched = handled, total = 10).reportingSlot }.distinct()

        assertEquals(10, emissions.size)
    }

    @Test
    fun reportingSlot_doesNotDivideByZero() {
        assertEquals(0, progress(total = 0).reportingSlot)
    }
}
