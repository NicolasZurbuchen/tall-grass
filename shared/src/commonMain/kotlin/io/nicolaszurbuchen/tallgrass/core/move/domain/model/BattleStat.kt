package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/**
 * A stat a move can move, in the order every screen draws stats in.
 *
 * **Not `PokemonStats`, and not an omission.** That record holds the six a Pokemon *has*; this is the
 * seven a move can *change*, which drops HP -- no move raises or lowers it in stages -- and adds
 * accuracy and evasion, which are not base stats at all. The two sets overlap without either
 * containing the other.
 *
 * The declaration order is load-bearing: `moveStatChange` has no ordering column, so this is where
 * the order lives. See `Move.sq`.
 */
enum class BattleStat(
    val slug: String,
) {
    ATTACK("attack"),
    DEFENSE("defense"),
    SPECIAL_ATTACK("special-attack"),
    SPECIAL_DEFENSE("special-defense"),
    SPEED("speed"),
    ACCURACY("accuracy"),
    EVASION("evasion"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): BattleStat? = bySlug[slug]
    }
}
