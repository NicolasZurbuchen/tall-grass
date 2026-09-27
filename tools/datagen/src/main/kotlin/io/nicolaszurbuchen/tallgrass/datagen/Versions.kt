package io.nicolaszurbuchen.tallgrass.datagen

/**
 * The console a version was played on, which is how the availability grid groups its cells.
 *
 * Not the generation. #9 settled this on the author's reasoning -- *"if I play Diamond, I may not
 * know it is the fourth generation, but I know I am playing on the DS"* -- and grouping by generation
 * needed eight rows and scattered the Switch games across three of them.
 *
 * Five rows, which is the five #9 named. There is no GameCube row, because the only GameCube games
 * are Colosseum and XD and this app covers the main series only -- see [SPIN_OFFS].
 */
enum class Console {
    SWITCH,
    THREE_DS,
    DS,
    GBA,
    GB_GBC,
}

/**
 * Console follows the generation, except once.
 *
 * Let's Go is a Generation VII pair that shipped on the Switch. Keyed by version group so the
 * fallback below stays a rule rather than a list of everything.
 */
private val CONSOLE_OVERRIDES: Map<String, Console> =
    mapOf("lets-go-pikachu-lets-go-eevee" to Console.SWITCH)

fun consoleOf(
    versionGroup: String,
    generation: Int,
): Console =
    CONSOLE_OVERRIDES[versionGroup] ?: when (generation) {
        1, 2 -> Console.GB_GBC
        3 -> Console.GBA
        4, 5 -> Console.DS
        6, 7 -> Console.THREE_DS
        else -> Console.SWITCH
    }

/**
 * The token drawn in a version's cell.
 *
 * Curated because deriving it collides: initials give Sword and Shield the same `S`, and upstream has
 * no short-name field to fall back on. #9 requires only that a code be unique **within its console
 * row** -- `Y` is Yellow on the GB row and Pokemon Y on the 3DS row, `B` is Blue on one and Black on
 * another -- which is what makes two characters enough for nearly all of them.
 *
 * The six downloadable chapters take three, built from their parent plus one letter, because they
 * share a row with the games they extend and a reader picking out `SwA` needs to see the `Sw` in it.
 */
private val VERSION_CODES: Map<String, String> =
    mapOf(
        // GB / GBC
        "red" to "R",
        "blue" to "B",
        "yellow" to "Y",
        "gold" to "G",
        "silver" to "S",
        "crystal" to "C",
        // GBA
        "ruby" to "R",
        "sapphire" to "S",
        "emerald" to "E",
        "firered" to "FR",
        "leafgreen" to "LG",
        // DS
        "diamond" to "D",
        "pearl" to "P",
        "platinum" to "Pt",
        "heartgold" to "HG",
        "soulsilver" to "SS",
        "black" to "B",
        "white" to "W",
        "black-2" to "B2",
        "white-2" to "W2",
        // 3DS
        "x" to "X",
        "y" to "Y",
        "omega-ruby" to "OR",
        "alpha-sapphire" to "AS",
        "sun" to "Su",
        "moon" to "Mo",
        "ultra-sun" to "US",
        "ultra-moon" to "UM",
        // Switch
        "lets-go-pikachu" to "LP",
        "lets-go-eevee" to "LE",
        "sword" to "Sw",
        "shield" to "Sh",
        "the-isle-of-armor-sword" to "SwA",
        "the-isle-of-armor-shield" to "ShA",
        "the-crown-tundra-sword" to "SwT",
        "the-crown-tundra-shield" to "ShT",
        "brilliant-diamond" to "BD",
        "shining-pearl" to "SP",
        "legends-arceus" to "LA",
        "scarlet" to "Sc",
        "violet" to "Vi",
        "the-teal-mask-scarlet" to "ScM",
        "the-teal-mask-violet" to "ViM",
        "the-indigo-disk-scarlet" to "ScD",
        "the-indigo-disk-violet" to "ViD",
        "legends-za" to "ZA",
        "mega-dimension" to "MD",
        "champions" to "Ch",
    )

/**
 * The three Japan-only Generation I releases.
 *
 * Dropped rather than drawn. They carry real encounter rows, but for anyone reading this app in
 * English they are the same games as Red and Blue, and keeping them would put three cells on Kanto's
 * grid that cannot be told apart from the three beside them. #9's count of "~37 cells" is the count
 * without them.
 */
private val JAPAN_ONLY: Set<String> = setOf("red-japan", "green-japan", "blue-japan")

/**
 * Colosseum and XD, the two GameCube games.
 *
 * **This app is a main-series Pokedex**, and these are spin-offs: a different developer, a different
 * genre, and no wild grass to speak of. Their 187 encounter rows and the region that exists only for
 * them -- Orre -- are not generated, which also means there is no GameCube row on any grid.
 *
 * Dropped here rather than filtered further down, so a spin-off cannot reach the dataset by any other
 * route: a version that is not shipped has no cell, and a region whose only games are unshipped has
 * no grid to put one on.
 */
private val SPIN_OFFS: Set<String> = setOf("colosseum", "xd")

fun isShippedVersion(slug: String): Boolean = slug !in JAPAN_ONLY && slug !in SPIN_OFFS && slug in VERSION_CODES

fun versionCodeOf(slug: String): String = VERSION_CODES.getValue(slug)
