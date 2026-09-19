package io.nicolaszurbuchen.tallgrass.infra.image

import coil3.PlatformContext
import okio.Path

/**
 * Where the operating system wants throwaway files kept.
 *
 * A platform seam because there is no cross-platform notion of it: Android hangs one off the
 * `Context`, iOS puts one in the app container. Both are directories the system may empty when the
 * device is short of space, which is the correct home for bytes the app can always fetch again.
 */
expect fun platformCacheDirectory(context: PlatformContext): Path

/**
 * How much room is left on the volume [path] sits on.
 *
 * The prefetch needs this because "the disk filled up" is otherwise something it can only learn by
 * failing: a write that runs out of space surfaces as an `IOException` whose message differs by
 * platform and by filesystem, and matching on that string is not a check, it is a guess.
 *
 * Asked about a directory that exists — the cache root, never a subdirectory the app may not have
 * created yet.
 */
expect fun availableBytesAt(path: Path): Long

/**
 * A directory of our own inside the platform's cache root, never the root itself: Coil clears the
 * directory it is given, and the root is shared with whatever else the system put there.
 */
fun imageCacheDirectory(cacheRoot: Path): Path = cacheRoot / IMAGE_CACHE_DIRECTORY_NAME

internal const val IMAGE_CACHE_DIRECTORY_NAME = "image_cache"
