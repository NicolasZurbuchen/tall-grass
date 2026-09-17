package io.nicolaszurbuchen.tallgrass.datagen

import java.io.File
import java.net.URI

/** A SHA rather than a branch, so the same pin regenerates the same output. Bumping it is the refresh. */
const val SOURCE_SHA: String = "4b82c204ddd19ecb8eda2ea044ccb59e222b721c"

/** Bumped when the shape of the generated JSON changes, not when its contents do. */
const val SCHEMA_VERSION: Int = 1

/** Upstream ships every name in a dozen languages in one file; unfiltered reads look like duplicates. */
const val ENGLISH: Int = 9

/** 19 is Stellar, a Terastal type no Pokemon has; 10001 and up are placeholders. */
const val LAST_REAL_TYPE_ID: Int = 18

private const val RAW_BASE = "https://raw.githubusercontent.com/PokeAPI/pokeapi"

private const val ARTWORK_BASE =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork"

/**
 * Artwork is referenced, never downloaded. Tracks `master` rather than a pinned SHA: this string is
 * read months later on a device and is the URL upstream's own API hands out.
 */
fun artworkUrl(id: Int): String = "$ARTWORK_BASE/$id.png"

/** Reads one upstream CSV, caching it under `cacheDir` keyed by [SOURCE_SHA]. */
class UpstreamSource(
    private val cacheDir: File,
) {
    fun read(name: String): List<CsvRow> {
        val cached = File(cacheDir, "$SOURCE_SHA/$name.csv")
        if (!cached.exists()) {
            cached.parentFile.mkdirs()
            println("  fetching $name.csv")
            cached.writeText(URI("$RAW_BASE/$SOURCE_SHA/data/v2/csv/$name.csv").toURL().readText())
        }
        return parseCsv(cached.readText())
    }
}
