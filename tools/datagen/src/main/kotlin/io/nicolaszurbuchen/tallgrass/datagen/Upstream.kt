package io.nicolaszurbuchen.tallgrass.datagen

import java.io.File
import java.net.URI

/** A SHA rather than a branch, so the same pin regenerates the same output. Bumping it is the refresh. */
const val SOURCE_SHA: String = "4b82c204ddd19ecb8eda2ea044ccb59e222b721c"

/** Bumped when the shape of the generated JSON changes, not when its contents do. */
const val SCHEMA_VERSION: Int = 3

/**
 * Upstream numbers spin-off content from 10000 in the same tables as the main series. Sixty of the
 * 374 abilities are Pokemon Conquest's and eighteen of the 937 moves are Pokemon XD's Shadow moves;
 * none of them has effect text, and no Pokemon in this dataset has one.
 *
 * The same threshold as `pokemon_forms`, and for an unrelated reason -- there it separates a
 * species' base form from its variants. Both are upstream's one convention for "not the ordinary
 * thing", applied to different tables.
 */
const val FIRST_SPIN_OFF_ID: Int = 10000

/** Upstream ships every name in a dozen languages in one file; unfiltered reads look like duplicates. */
const val ENGLISH: Int = 9

/** 19 is Stellar, a Terastal type no Pokemon has; 10001 and up are placeholders. */
const val LAST_REAL_TYPE_ID: Int = 18

private const val RAW_BASE = "https://raw.githubusercontent.com/PokeAPI/pokeapi"

private const val SPRITE_BASE =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other"

private const val ARTWORK_BASE = "$SPRITE_BASE/official-artwork"

private const val HOME_BASE = "$SPRITE_BASE/home"

/**
 * Artwork is referenced, never downloaded. Tracks `master` rather than a pinned SHA: this string is
 * read months later on a device and is the URL upstream's own API hands out.
 *
 * [id] is a **`pokemon` id**, which is not the same number as a `pokemon_forms` id even though both
 * run into the ten-thousands. Passing the wrong one produces a URL that resolves — to a different
 * Pokemon. See [formArtworkUrl].
 */
fun artworkUrl(id: Int): String = "$ARTWORK_BASE/$id.png"

/**
 * The HOME render of one form, for the forms that have no `pokemon` row and therefore no official
 * artwork: `official-artwork/493-fighting.png` is a 404, while `home/493-fighting.png` is Arceus
 * holding the Fist Plate.
 *
 * A second art style in an app built around official artwork, which is the price of showing all
 * eighteen Arceus as themselves. It reaches only the detail hero, because none of these forms earns
 * a card in the grid.
 */
fun formArtworkUrl(
    pokemonId: Int,
    formIdentifier: String,
): String = "$HOME_BASE/$pokemonId-$formIdentifier.png"

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
