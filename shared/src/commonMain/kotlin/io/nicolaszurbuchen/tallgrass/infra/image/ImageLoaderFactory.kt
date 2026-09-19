package io.nicolaszurbuchen.tallgrass.infra.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import io.ktor.client.HttpClient

/**
 * One image loader for the whole app, configured with a disk cache.
 *
 * Coil builds an `ImageLoader` with `diskCache = null` unless told otherwise, which is the whole
 * reason this factory exists rather than the singleton default.
 *
 * [httpClient] is shared so image requests go through the same engine and timeouts as everything
 * else. [crossfadeMillis] is passed in because this is `infra/` and the duration is a design token.
 *
 * DECISIONS.md § The artwork corpus is 133 MB, and the disk cache is sized against it
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

// 192 MB: above the 132.6 MB prefetched set, or later images evict earlier ones and the run undoes
// itself, with headroom for the forms that are fetched lazily.
// DECISIONS.md § The artwork corpus is 133 MB, and the disk cache is sized against it
private const val MAX_DISK_CACHE_BYTES = 192L * 1024 * 1024
