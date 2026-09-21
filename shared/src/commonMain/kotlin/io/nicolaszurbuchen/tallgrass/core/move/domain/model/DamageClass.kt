package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/**
 * How a move deals its damage, or that it does not.
 *
 * Three members and closed since Generation IV, when the split stopped being a property of the type
 * and became a property of the move. It is the first thing a player checks after the type, because a
 * 90-power Flamethrower and a 90-power Close Combat are answered by different defences.
 *
 * [slug] is the dataset's spelling, which is also upstream's.
 */
enum class DamageClass(
    val slug: String,
) {
    PHYSICAL("physical"),
    SPECIAL("special"),
    STATUS("status"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): DamageClass? = bySlug[slug]
    }
}
