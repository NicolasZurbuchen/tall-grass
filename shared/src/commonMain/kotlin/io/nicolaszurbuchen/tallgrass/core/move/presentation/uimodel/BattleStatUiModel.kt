package io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel

/**
 * A stat a move moves, abbreviated the way the stat table on a Pokemon abbreviates it. The two sets
 * are not the same -- this one has no HP and adds accuracy and evasion -- but where they overlap they
 * read identically, because it is the same stat.
 */
enum class BattleStatUiModel(
    val label: String,
) {
    ATTACK("Attack"),
    DEFENSE("Defense"),
    SPECIAL_ATTACK("Sp. Atk"),
    SPECIAL_DEFENSE("Sp. Def"),
    SPEED("Speed"),
    ACCURACY("Accuracy"),
    EVASION("Evasion"),
}
