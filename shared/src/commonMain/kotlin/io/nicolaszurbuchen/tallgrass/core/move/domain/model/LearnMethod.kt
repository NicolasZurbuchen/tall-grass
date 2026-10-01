package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/**
 * How a Pokemon comes by a move.
 *
 * Four of upstream's twelve. The other eight are either one game's curiosity -- a surfing Pikachu in
 * Stadium, a Light Ball egg -- or not a way of learning anything at all: `train` is Legends: Arceus's
 * move mastery, which sharpens a move the Pokemon already has, and reading it as a method leaves the
 * 319 Pokemon whose newest game is Pokemon Champions with an empty learnset. See `DECISIONS.md` on
 * the learnset.
 *
 * The declaration order is the generator's preference when a Pokemon has more than one way to the
 * same move: [LEVEL_UP] first because it is the one that carries a number.
 */
enum class LearnMethod(
    val slug: String,
) {
    LEVEL_UP("level-up"),
    MACHINE("machine"),
    EGG("egg"),
    TUTOR("tutor"),

    /**
     * Not one of upstream's and not a way of learning anything: a Gigantamax form has its G-Max Move
     * by being that form, the way it has its types.
     *
     * It is a learnset row all the same, because "which Pokemon has this move" has one table and
     * that is it. Last in the declaration because the order is the generator's preference between
     * two ways to the same move, and there is never a second way to this one.
     */
    GIGANTAMAX("gigantamax"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): LearnMethod? = bySlug[slug]
    }
}
