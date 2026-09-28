package io.nicolaszurbuchen.tallgrass.datagen

/**
 * What kind of place a location is, for the coloured marker beside it in a region's Locations list.
 *
 * **Upstream has no such field.** There is nothing in `locations.csv` but an id, a region and an
 * identifier, and no other table classifies a place. So this is read out of the slug, which is the
 * only signal there is -- and it is a presentation nicety rather than a fact, which is why a miss
 * costs nothing: an unrecognised place gets [OTHER] and the marker goes neutral.
 *
 * That is also why the keyword lists are long and unapologetically shallow. They were tuned against
 * all 1,013 region-bearing locations until 83% matched something; chasing the rest would mean
 * hand-reading names like `spear-pillar` and `sendoff-spring`, which is curation this does not earn.
 */
enum class LocationCategory {
    ROUTE,
    TOWN,
    CAVE,
    FOREST,
    WATER,
    MOUNTAIN,
    BUILDING,
    PARK,
    OTHER,
}

/**
 * Order matters and is not alphabetical.
 *
 * `-tunnel` is a cave and `rock-tunnel-shop` is a building, so the narrower reading has to be tried
 * first wherever two lists can both match. The same goes for `route` ahead of everything: `sea-route`
 * exists in Alola and is a route a reader walks, not a stretch of water.
 *
 * Written as space-separated words rather than as lists of string literals. These are vocabulary
 * rather than code, and one keyword per line would run the table past two hundred lines.
 */
private val KEYWORDS: List<Pair<LocationCategory, List<String>>> =
    listOf(
        LocationCategory.ROUTE to "route -path road trail bridge street avenue",
        LocationCategory.TOWN to "city town village metropolis borough",
        LocationCategory.CAVE to "cave tunnel mine cavern grotto chamber -den hollow abyss depths burrow",
        LocationCategory.FOREST to "forest woods grove jungle thicket arbor orchard",
        LocationCategory.WATER to
            "sea ocean lake river bay beach shore island isle water falls coast well marsh swamp spring " +
            "pond reef cove lagoon harbor harbour wharf canal",
        LocationCategory.MOUNTAIN to
            "mt- mount peak summit hill volcano slope crater cliff ridge highland ascent",
        LocationCategory.BUILDING to
            "tower building lab gym center centre house mansion castle plant factory ruins tomb temple room " +
            "station gate league chateau works institute school shop market dojo club hotel dome arena " +
            "stadium hall ship train museum library studio cafe office mart depot fort",
        LocationCategory.PARK to
            "park garden zone meadow field plains prairie savanna area square paradise ranch farm yard court " +
            "plaza terrace valley canyon desert tundra glacier wilds moor",
    ).map { (category, words) -> category to words.split(" ") }

fun locationCategoryOf(slug: String): LocationCategory =
    KEYWORDS.firstOrNull { (_, words) -> words.any { it in slug } }
        ?.first
        ?: LocationCategory.OTHER
