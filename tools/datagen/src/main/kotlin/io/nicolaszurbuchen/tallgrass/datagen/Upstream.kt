package io.nicolaszurbuchen.tallgrass.datagen

import java.io.File
import java.net.URI

/**
 * The pinned upstream commit every generated file is derived from.
 *
 * A SHA rather than a branch, because "regenerate and get the same output" is only true against an
 * immovable source. Bumping this is the refresh trigger, and the resulting JSON diff is the review.
 */
const val SOURCE_SHA: String = "4b82c204ddd19ecb8eda2ea044ccb59e222b721c"

/** Bumped by hand when the shape of the generated JSON changes, not when its contents do. */
const val SCHEMA_VERSION: Int = 1

/**
 * English. Upstream ships every name in a dozen languages in the same file, keyed by this.
 *
 * The app is English-only for now, and a `*_names.csv` read without filtering yields one row per
 * language -- which looks like duplicate data rather than an error.
 */
const val ENGLISH: Int = 9

/** The eighteen real types. 19 is Stellar, a Terastal type no Pokemon is; 10001+ are placeholders. */
const val LAST_REAL_TYPE_ID: Int = 18

private const val RAW_BASE = "https://raw.githubusercontent.com/PokeAPI/pokeapi"

/**
 * Official artwork lives in a separate repository and is referenced, never downloaded.
 *
 * Tracks `master` rather than a pinned SHA: this string is not read at build time but months later,
 * on a device, and it is the URL upstream's own API hands out. Pinning it would freeze the artwork
 * of every Pokemon to whatever it looked like on generation day, to buy reproducibility of a file
 * this repository never stores.
 */
private const val ARTWORK_BASE =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork"

fun artworkUrl(pokemonId: Int): String = "$ARTWORK_BASE/$pokemonId.png"

/**
 * Reads one upstream CSV, caching it under `cacheDir`.
 *
 * The cache is keyed by the pinned SHA, so bumping [SOURCE_SHA] fetches afresh rather than serving a
 * stale file, and re-running the generator against an unchanged pin touches the network once.
 */
class UpstreamSource(
    private val cacheDir: File,
) {
    fun read(name: String): List<CsvRow> {
        val cached = File(cacheDir, "$SOURCE_SHA/$name.csv")
        if (!cached.exists()) {
            cached.parentFile.mkdirs()
            val url = "$RAW_BASE/$SOURCE_SHA/data/v2/csv/$name.csv"
            println("  fetching $name.csv")
            cached.writeText(URI(url).toURL().readText())
        }
        return parseCsv(cached.readText())
    }
}
