package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * The fifteen breeding groups. Closed since Generation II, so a sixteenth is a bad row rather than a
 * case to handle.
 *
 * [slug] is upstream's spelling, which is not the name the games use — `plant` is Grass and `ground`
 * is Field. The two vocabularies are kept apart on purpose: this is the key the dataset stores, and
 * what a player would recognise is a rendering decision made on the way out.
 */
enum class EggGroup(
    val slug: String,
) {
    MONSTER("monster"),
    WATER_1("water1"),
    BUG("bug"),
    FLYING("flying"),
    FIELD("ground"),
    FAIRY("fairy"),
    GRASS("plant"),
    HUMAN_LIKE("humanshape"),
    WATER_3("water3"),
    MINERAL("mineral"),
    AMORPHOUS("indeterminate"),
    WATER_2("water2"),
    DITTO("ditto"),
    DRAGON("dragon"),
    UNDISCOVERED("no-eggs"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): EggGroup? = bySlug[slug]
    }
}
