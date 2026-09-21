package io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel

/**
 * What a move is aimed at, said in a phrase rather than in upstream's hyphenated key.
 *
 * [SPECIFIC_MOVE] is the odd one and reads oddly on purpose: the five moves that carry it -- Counter,
 * Mirror Coat, Metal Burst and their kin -- target a move that was used rather than a Pokemon.
 */
enum class MoveTargetUiModel(
    val label: String,
) {
    SPECIFIC_MOVE("A move"),
    SELECTED_POKEMON_ME_FIRST("One target"),
    ALLY("An ally"),
    USERS_FIELD("Your side"),
    USER_OR_ALLY("Itself or an ally"),
    OPPONENTS_FIELD("The opposing side"),
    USER("Itself"),
    RANDOM_OPPONENT("A random opponent"),
    ALL_OTHER_POKEMON("Everyone else"),
    SELECTED_POKEMON("One target"),
    ALL_OPPONENTS("All opponents"),
    ENTIRE_FIELD("The whole field"),
    USER_AND_ALLIES("Itself and its allies"),
    ALL_POKEMON("Everyone"),
    ALL_ALLIES("All allies"),
    FAINTING_POKEMON("A fainting Pokémon"),
}
