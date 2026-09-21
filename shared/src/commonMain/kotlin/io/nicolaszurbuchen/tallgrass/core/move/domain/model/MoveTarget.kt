package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/**
 * What a move is aimed at. Sixteen, and closed upstream.
 *
 * Most of the list is rare -- 656 of the 919 moves are [SELECTED_POKEMON] and nine of the members
 * are on fewer than ten moves each -- but a target that fell through to a default would be a factual
 * claim about the move rather than a blank, so all sixteen are named.
 */
enum class MoveTarget(
    val slug: String,
) {
    SPECIFIC_MOVE("specific-move"),
    SELECTED_POKEMON_ME_FIRST("selected-pokemon-me-first"),
    ALLY("ally"),
    USERS_FIELD("users-field"),
    USER_OR_ALLY("user-or-ally"),
    OPPONENTS_FIELD("opponents-field"),
    USER("user"),
    RANDOM_OPPONENT("random-opponent"),
    ALL_OTHER_POKEMON("all-other-pokemon"),
    SELECTED_POKEMON("selected-pokemon"),
    ALL_OPPONENTS("all-opponents"),
    ENTIRE_FIELD("entire-field"),
    USER_AND_ALLIES("user-and-allies"),
    ALL_POKEMON("all-pokemon"),
    ALL_ALLIES("all-allies"),
    FAINTING_POKEMON("fainting-pokemon"),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        fun fromSlug(slug: String): MoveTarget? = bySlug[slug]
    }
}
