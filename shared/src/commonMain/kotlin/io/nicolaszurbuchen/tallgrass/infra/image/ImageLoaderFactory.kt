package io.nicolaszurbuchen.tallgrass.infra.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import io.ktor.client.HttpClient

/**
 * One image loader for the whole app, built to survive a device with no signal.
 *
 * **The disk cache is configured rather than left to a default, because there is no default.** Coil
 * builds an `ImageLoader` with `diskCache = null` unless told otherwise, so until now every one of
 * the dex's artworks was re-fetched on each cold start. Nothing else in this app touches the
 * network, which made that the whole of its offline story.
 *
 * The memory cache stays on Coil's default. It is sized against the device's own memory and only
 * ever affects a session; the disk is the part that decides whether the dex works on a train.
 *
 * Sharing [httpClient] rather than letting Coil build its own puts image requests through the same
 * engine and timeouts as everything else — which here means the one Ktor client the app has.
 *
 * [crossfadeMillis] is passed in rather than read from a token, because this is `infra/` and the
 * duration is a design decision. See #12.
 */
fun createImageLoader(
    context: PlatformContext,
    httpClient: HttpClient,
    crossfadeMillis: Int,
): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(KtorNetworkFetcherFactory(httpClient = { httpClient })) }
        .diskCache {
            DiskCache.Builder()
                .directory(imageCacheDirectory(platformCacheDirectory(context)))
                .maxSizeBytes(MAX_DISK_CACHE_BYTES)
                .build()
        }
        // Explicit rather than inherited: reading and writing the disk is the whole point of the
        // block above, and a policy that changed under us would be invisible until someone with no
        // signal saw empty cards.
        .diskCachePolicy(CachePolicy.ENABLED)
        .crossfade(crossfadeMillis)
        .build()

/**
 * 192 MB, against a measured corpus of **132.6 MB for the 1,082 dex cards** and 166.2 MB for all
 * 1,385 variants including the forms only the detail switcher reaches.
 *
 * Measured rather than guessed: every artwork URL in the committed dataset was asked for its length.
 * The average is 123 KB and the largest single image is 289 KB.
 *
 * The ceiling has to clear the prefetched set or the cache thrashes — later images evict earlier
 * ones and the run is self-defeating — and the headroom above it covers the forms, which are fetched
 * lazily and would otherwise start evicting cards.
 *
 * A byte cap rather than a percentage of free space, because this corpus has a knowable size: a
 * percentage hands a 512 GB phone a quota nothing will ever fill, and a nearly-full phone one too
 * small to be worth writing to.
 */
private const val MAX_DISK_CACHE_BYTES = 192L * 1024 * 1024
