package io.nicolaszurbuchen.tallgrass.datagen

/**
 * The three things about a region that upstream does not carry.
 *
 * Everything else on the region screens is derived -- generation, native name, location count, the
 * games set here, the regional dex itself. These three are not derivable at all, and #24 accepted
 * the curation because eleven rows is a table somebody can read in one sitting and check.
 *
 * [boxArt] is the pair of variants on the region's card. A single defining mascot was rejected as an
 * arbitrary editorial pick -- there is no argument for Ho-Oh over Lugia -- and the pair removes the
 * choice. There is no API field for box art, so the ids were verified by hand.
 *
 * [blurb] is original prose. A region's own description would be flavour text, which #10 and #11
 * forbid shipping; written here instead, it is the project's to ship. Eleven of them is the whole
 * cost, and unlike the 1,025 species blurbs the same argument would imply, it finishes.
 */
data class CuratedRegion(
    val slug: String,
    val boxArt: List<String>,
    val pokedexes: List<String>,
    val blurb: String,
)

/**
 * Which upstream Pokedex is *the* Pokedex of a region.
 *
 * Upstream gives a region as many as ten. The rule taken here is **the dex as the region's first
 * games shipped it**: `original-johto` rather than the 256-entry HeartGold revision, `original-alola`
 * rather than the Ultra expansion, `galar` without its two downloadable dexes. That is what a reader
 * means by "the Johto Pokedex", and it is a rule rather than a taste call, so a new region classifies
 * itself.
 *
 * Kalos is the one region with no single dex at all -- it ships three, and any one of them is a third
 * of the region -- so it is the reason this is a list. Orre is the other end: Colosseum and XD have no
 * regional dex upstream, and an empty list is the honest answer rather than a substituted one.
 */
val CURATED_REGIONS: List<CuratedRegion> =
    listOf(
        CuratedRegion(
            slug = "kanto",
            boxArt = listOf("charizard", "blastoise"),
            pokedexes = listOf("kanto"),
            blurb =
                "The region the series began in: a peninsula of quiet towns and dense forest strung along a " +
                    "coast, anchored by a volcanic island in the south. Small enough to cross on foot, and the " +
                    "only region a later generation ever sent anyone back to.",
        ),
        CuratedRegion(
            slug = "johto",
            boxArt = listOf("ho-oh", "lugia"),
            pokedexes = listOf("original-johto"),
            blurb =
                "West of Kanto and older than it, a region of shrine towns, bamboo and bell towers, where the " +
                    "series first put its legends into its scenery rather than at the end of a cave. The two " +
                    "regions connect by rail and by sea, and one journey runs into the other.",
        ),
        CuratedRegion(
            slug = "hoenn",
            boxArt = listOf("groudon", "kyogre"),
            pokedexes = listOf("hoenn"),
            blurb =
                "Half of this region is water. An archipelago of rainforest, desert and a dormant volcano, built " +
                    "around a quarrel between the sea and the land, and the first place where going under the " +
                    "surface was part of the map instead of a detour.",
        ),
        CuratedRegion(
            slug = "sinnoh",
            boxArt = listOf("dialga", "palkia"),
            pokedexes = listOf("original-sinnoh"),
            blurb =
                "A cold northern landmass split down the middle by a mountain range that nothing crosses easily. " +
                    "Snow at the top, marsh and mining towns below, and a summit where the region keeps its account " +
                    "of how time and space were set going.",
        ),
        CuratedRegion(
            slug = "unova",
            boxArt = listOf("reshiram", "zekrom"),
            pokedexes = listOf("original-unova"),
            blurb =
                "A region built around one enormous city, with bridges where other regions have routes and seasons " +
                    "that redraw the map four times a year. Far from the rest in every sense: the first region whose " +
                    "early wildlife shares nothing at all with anywhere else.",
        ),
        CuratedRegion(
            slug = "kalos",
            boxArt = listOf("xerneas", "yveltal"),
            pokedexes = listOf("kalos-central", "kalos-coastal", "kalos-mountain"),
            blurb =
                "Three lobes around a central city of iron and glass, with a southern coast, a northern range, and " +
                    "more of its identity invested in style than any region before it. Also where the eighteenth " +
                    "type arrived and the whole chart had to be rewritten.",
        ),
        CuratedRegion(
            slug = "alola",
            boxArt = listOf("solgaleo", "lunala"),
            pokedexes = listOf("original-alola"),
            blurb =
                "Four tropical islands and a man-made fifth, where the journey is a series of island trials rather " +
                    "than a circuit of gyms. Familiar species turn up here wearing different types, which is the " +
                    "region's whole argument about what a place does to what lives in it.",
        ),
        CuratedRegion(
            slug = "galar",
            boxArt = listOf("zacian", "zamazenta"),
            pokedexes = listOf("galar"),
            blurb =
                "An industrial region of moors, mining towns and a stadium circuit followed by the entire country " +
                    "at once. Its open wild area changes weather in front of you, and what is standing in the grass " +
                    "changes with it.",
        ),
        CuratedRegion(
            slug = "hisui",
            // Arceus alone. The only region whose games shipped without a legendary pair on the cover, so the
            // pair rule has nothing to reach for and the fallback is the single mascot it does have.
            boxArt = listOf("arceus"),
            pokedexes = listOf("hisui"),
            blurb =
                "Sinnoh long before it was Sinnoh: unsurveyed, unsettled, and hostile in a way the later map is " +
                    "not. There is less a dex to fill in here than one to write, and the wildlife has not yet " +
                    "learned to ignore people.",
        ),
        CuratedRegion(
            slug = "paldea",
            boxArt = listOf("koraidon", "miraidon"),
            pokedexes = listOf("paldea"),
            blurb =
                "A wide open region with a crater at its centre, crossed in whatever order the traveller likes. " +
                    "Three separate stories run across the same map at once, and the land between them is " +
                    "continuous rather than routed.",
        ),
        CuratedRegion(
            slug = "orre",
            // Espeon and Umbreon, from Colosseum's cover -- the region has no legendary pair either.
            boxArt = listOf("espeon", "umbreon"),
            // Upstream has no Orre dex. The Pokedex tab says so rather than substituting the national one.
            pokedexes = emptyList(),
            blurb =
                "A desert region off the main sequence, with almost no wild grass in it at all. Most of what lives " +
                    "here has already been taken and mistreated by somebody else, so the work is snatching Pokemon " +
                    "back rather than catching them.",
        ),
    )
