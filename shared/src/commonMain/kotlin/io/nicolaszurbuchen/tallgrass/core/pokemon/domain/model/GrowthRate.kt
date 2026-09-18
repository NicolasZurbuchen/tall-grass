package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * How much experience a species needs to level.
 *
 * Six values, and two of them are named after their curve upstream rather than after what the games
 * call them: `slow-then-very-fast` is Erratic and `fast-then-very-slow` is Fluctuating.
 */
enum class GrowthRate(
    val slug: String,
) {
    SLOW("slow"),
    MEDIUM_SLOW("medium-slow"),
    MEDIUM_FAST("medium"),
    FAST("fast"),
    ERRATIC("slow-then-very-fast"),
    FLUCTUATING("fast-then-very-slow"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): GrowthRate? = bySlug[slug]
    }
}
