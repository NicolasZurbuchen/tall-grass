package io.nicolaszurbuchen.tallgrass.infra.image

import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * [ImagePrefetch] against the one loader the app installs, reached the way Coil intends rather than
 * by holding a second reference to it. The cache filled here is by construction the cache every
 * `AsyncImage` in the app reads.
 */
class CoilImagePrefetch(
    private val context: PlatformContext,
) : ImagePrefetch {
    override fun run(urls: List<String>): Flow<ImagePrefetchProgress> =
        flow {
            val loader = SingletonImageLoader.get(context)
            val diskCache = loader.diskCache

            // Nothing to fill. A build with the disk cache turned off would otherwise download the
            // whole corpus into memory and throw it away.
            if (diskCache == null) {
                emit(ImagePrefetchProgress(fetched = 0, alreadyCached = 0, failed = 0, total = 0))
                return@flow
            }

            val cacheRoot = platformCacheDirectory(context)
            var progress = ImagePrefetchProgress(fetched = 0, alreadyCached = 0, failed = 0, total = urls.size)
            var lastReportedSlot = -1

            urls.forEachIndexed { index, url ->
                // Checked on the way in and then every so often, not before every image: it is a
                // filesystem call, and the answer cannot change much in the time one 123 KB picture
                // takes to arrive.
                if (index % SPACE_CHECK_INTERVAL == 0 && availableBytesAt(cacheRoot) < MINIMUM_FREE_BYTES) {
                    emit(progress.copy(stoppedForSpace = true))
                    return@flow
                }

                // Asking the cache rather than remembering a cursor. This is what makes the run
                // resumable across a kill: what is on disk is the progress, and no record of it can
                // disagree with the disk about what is really there.
                val cached = diskCache.openSnapshot(url)?.also { it.close() } != null

                progress =
                    if (cached) {
                        progress.copy(alreadyCached = progress.alreadyCached + 1)
                    } else {
                        when (fetch(url)) {
                            true -> progress.copy(fetched = progress.fetched + 1)
                            false -> progress.copy(failed = progress.failed + 1)
                        }
                    }

                // Not once per image. See ImagePrefetchProgress.reportingSlot.
                val slot = progress.reportingSlot

                if (slot != lastReportedSlot || progress.handled == progress.total) {
                    lastReportedSlot = slot
                    emit(progress)
                }
            }
        }.flowOn(Dispatchers.Default)

    // A failure is counted and swallowed: one 404 must not end the run, and there is nothing a
    // reader could do about it if it were reported.
    private suspend fun fetch(url: String): Boolean =
        try {
            val request =
                ImageRequest.Builder(context)
                    .data(url)
                    // Decoded, this corpus is far larger than it is on disk, so warming the memory cache would
                    // evict the bitmaps the screen in front of the reader is using.
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build()

            SingletonImageLoader.get(context).execute(request) is SuccessResult
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
}

// Every fiftieth image, which at ~123 KB each is about 6 MB of writing between checks -- far less
// than the floor below, so the run cannot cross it unnoticed.
private const val SPACE_CHECK_INTERVAL = 50

/**
 * 64 MB. The prefetch stops here rather than at zero, because the device does not belong to it:
 * filling the last megabyte of someone's phone with Pokemon pictures is worse behaviour than
 * stopping early with a dex that fetches the rest lazily.
 */
private const val MINIMUM_FREE_BYTES = 64L * 1024 * 1024
