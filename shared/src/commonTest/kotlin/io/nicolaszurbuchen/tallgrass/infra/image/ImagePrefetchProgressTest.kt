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
}
