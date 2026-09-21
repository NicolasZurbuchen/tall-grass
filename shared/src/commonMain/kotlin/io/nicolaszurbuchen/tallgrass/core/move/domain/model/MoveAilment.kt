package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/**
 * The condition a move inflicts.
 *
 * Wider than the status conditions a player would list: upstream files Leech Seed, Ingrain and even
 * Protect here, because the question it answers is "what does this move put on something" rather
 * than "which of the five statuses is it".
 *
 * [UNKNOWN] is upstream's own `-1` and belongs to the four moves whose ailment genuinely varies --
 * Tri Attack picks one of burn, freeze and paralysis. It is a value rather than a null because the
 * chance beside it is real: dropping it would strand Tri Attack's 20%.
 */
enum class MoveAilment(
    val slug: String,
) {
    PARALYSIS("paralysis"),
    SLEEP("sleep"),
    FREEZE("freeze"),
    BURN("burn"),
    POISON("poison"),
    CONFUSION("confusion"),
    INFATUATION("infatuation"),
    TRAP("trap"),
    NIGHTMARE("nightmare"),
    TORMENT("torment"),
    DISABLE("disable"),
    YAWN("yawn"),
    HEAL_BLOCK("heal-block"),
    NO_TYPE_IMMUNITY("no-type-immunity"),
    LEECH_SEED("leech-seed"),
    EMBARGO("embargo"),
    PERISH_SONG("perish-song"),
    INGRAIN("ingrain"),
    SILENCE("silence"),
    TAR_SHOT("tar-shot"),
    PROTECT("protect"),
    UNKNOWN("unknown"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): MoveAilment? = bySlug[slug]
    }
}
