package io.nicolaszurbuchen.tallgrass.infra.image

import android.os.StatFs
import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toOkioPath

actual fun platformCacheDirectory(context: PlatformContext): Path = context.cacheDir.toOkioPath()

// availableBytes rather than freeBytes: the second counts blocks the filesystem reserves for root
// and would have the prefetch keep going into space it cannot actually write to.
actual fun availableBytesAt(path: Path): Long = StatFs(path.toFile().absolutePath).availableBytes
