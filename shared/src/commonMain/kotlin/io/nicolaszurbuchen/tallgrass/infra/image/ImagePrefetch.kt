package io.nicolaszurbuchen.tallgrass.infra.image

import kotlinx.coroutines.flow.Flow

/**
 * Fills the image disk cache ahead of anyone asking for a picture.
 *
 * A port rather than a call to Coil from a screen, for the ordinary reason: a feature may talk to
 * `infra` and may not talk to a third-party singleton.
 *
 * **It never throws and it never blocks anything.** Every screen in this app already works without
 * it — the dataset is bundled — so a failed prefetch is a worse-looking dex rather than a broken
 * one, and that is reflected in the return type: the run reports what happened and finishes.
 */
interface ImagePrefetch {
    /**
     * Fetches each of [urls] that is not already on disk, emitting after each one.
     *
     * The flow always ends with a final [ImagePrefetchProgress] and never with an exception. It is
     * cold: nothing is fetched until it is collected, and cancelling the collector stops the run
     * with whatever it had already written left in place.
     */
    fun run(urls: List<String>): Flow<ImagePrefetchProgress>
}

/**
 * How far a run has got.
 *
 * [fetched] counts images written by this run; [alreadyCached] counts the ones it found and skipped,
 * which is how a resumed run explains itself. Together with [failed] they account for every URL up
 * to [handled].
 *
 * [stoppedForSpace] is the one ending that is not simply "finished": the run gave up because the
 * device is nearly full, and what it had already written stays.
 */
data class ImagePrefetchProgress(
    val fetched: Int,
    val alreadyCached: Int,
    val failed: Int,
    val total: Int,
    val stoppedForSpace: Boolean = false,
) {
    val handled: Int get() = fetched + alreadyCached + failed

    val isComplete: Boolean get() = handled >= total || stoppedForSpace

    /**
     * Zero to one. Reports against [total] rather than against what is left, so a resumed run starts
     * where the last one stopped instead of restarting the bar at zero.
     */
    val fraction: Float get() = if (total <= 0) 1f else (handled.toFloat() / total).coerceIn(0f, 1f)
}

// Emissions are not free: each one is a new state, and mapping it rebuilds every card in the dex.
// Twenty-five slots is already more than a percentage on one line of text can express.
// DECISIONS.md § The prefetch reports in slots, not per image
internal val ImagePrefetchProgress.reportingSlot: Int
    get() = if (total <= 0) 0 else handled * REPORTING_SLOTS / total

private const val REPORTING_SLOTS = 25
