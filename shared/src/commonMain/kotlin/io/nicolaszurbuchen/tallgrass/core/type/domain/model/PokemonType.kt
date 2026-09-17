package io.nicolaszurbuchen.tallgrass.core.type.domain.model

/**
 * The eighteen types.
 *
 * An enum rather than a string because the set is closed and has been since Generation VI: a type
 * that is not one of these is a bad row in the dataset, and failing to parse it is the right
 * outcome. Stellar is deliberately absent -- it is a Terastal type, not a type a Pokemon has.
 *
 * [slug] is the dataset's spelling, which is also upstream's, so the mapping needs no table.
 */
enum class PokemonType(
    val slug: String,
) {
    NORMAL("normal"),
    FIRE("fire"),
    WATER("water"),
    ELECTRIC("electric"),
    GRASS("grass"),
    ICE("ice"),
    FIGHTING("fighting"),
    POISON("poison"),
    GROUND("ground"),
    FLYING("flying"),
    PSYCHIC("psychic"),
    BUG("bug"),
    ROCK("rock"),
    GHOST("ghost"),
    DRAGON("dragon"),
    DARK("dark"),
    STEEL("steel"),
    FAIRY("fairy"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): PokemonType? = bySlug[slug]
    }
}
