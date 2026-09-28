package io.nicolaszurbuchen.tallgrass.datagen

/**
 * The console a version was played on, which is how the availability grid groups its cells.
 *
 * Not the generation. #9 settled this on the author's reasoning -- *"if I play Diamond, I may not
 * know it is the fourth generation, but I know I am playing on the DS"* -- and grouping by generation
 * needed eight rows and scattered the Switch games across three of them.
 *
 * [GAME_CUBE] is not one of the five rows #9 named, and it is here because Orre exists: Colosseum and
 * XD are the region's only games and they are neither GBA nor anything else on the list. Rows are
 * rendered only where a region has games, so it appears on Orre and nowhere else -- the same rule
 * that gives Kanto no 3DS row.
 */
enum class Console {
    SWITCH,
    THREE_DS,
    DS,
    GAME_CUBE,
    GBA,
    GB_GBC,
}

/**
 * Console follows the generation, except twice.
 *
 * Let's Go is a Generation VII pair that shipped on the Switch, and Colosseum and XD are Generation
 * III on the GameCube. Both are keyed by version group here so the fallback below stays a rule rather
 * than a list of everything.
 */
private val CONSOLE_OVERRIDES: Map<String, Console> =
    mapOf(
        "lets-go-pikachu-lets-go-eevee" to Console.SWITCH,
        "colosseum" to Console.GAME_CUBE,
        "xd" to Console.GAME_CUBE,
    )

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
        // GameCube
        "colosseum" to "Co",
        "xd" to "XD",
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
 * Dropped rather than drawn. They carry real encounter rows, but they are the same games as Red and
 * Blue for anyone reading this app in English, and keeping them would put three cells on Kanto's grid
 * that a reader cannot tell apart from the three beside them. #9's count of "~37 cells" is the count
 * without them.
 */
private val JAPAN_ONLY: Set<String> = setOf("red-japan", "green-japan", "blue-japan")

fun isShippedVersion(slug: String): Boolean = slug !in JAPAN_ONLY && slug in VERSION_CODES

fun versionCodeOf(slug: String): String = VERSION_CODES.getValue(slug)
