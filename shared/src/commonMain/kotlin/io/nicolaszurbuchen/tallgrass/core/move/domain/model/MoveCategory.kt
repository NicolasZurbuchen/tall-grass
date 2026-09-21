package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/**
 * What kind of thing a move is, in upstream's own fourteen-way split.
 *
 * **The only classification in this app that was not invented.** Three home-made taxonomies for the
 * abilities were built and thrown away because upstream has no such field for them; moves have one,
 * so this is read rather than derived. See `DECISIONS.md § Rejected for now: a classification for
 * abilities` and #65.
 *
 * It is not a tidy partition -- [UNIQUE] holds 106 moves and [SWAGGER] holds four -- but it is
 * upstream's untidiness rather than this app's, which is the whole reason it is trustworthy.
 */
enum class MoveCategory(
    val slug: String,
) {
    DAMAGE("damage"),
    AILMENT("ailment"),
    NET_GOOD_STATS("net-good-stats"),
    HEAL("heal"),
    DAMAGE_AILMENT("damage-ailment"),
    SWAGGER("swagger"),
    DAMAGE_LOWER("damage-lower"),
    DAMAGE_RAISE("damage-raise"),
    DAMAGE_HEAL("damage-heal"),
    OHKO("ohko"),
    WHOLE_FIELD_EFFECT("whole-field-effect"),
    FIELD_EFFECT("field-effect"),
    FORCE_SWITCH("force-switch"),
    UNIQUE("unique"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): MoveCategory? = bySlug[slug]
    }
}
