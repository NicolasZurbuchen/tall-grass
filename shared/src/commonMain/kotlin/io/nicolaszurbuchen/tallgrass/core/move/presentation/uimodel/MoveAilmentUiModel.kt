package io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel

/**
 * The condition a move inflicts.
 *
 * [UNKNOWN] is not a failure to read the data -- it is what upstream stores for the four moves whose
 * ailment varies, and "A status condition" is the honest rendering of it. Tri Attack really does pick
 * one of three.
 */
enum class MoveAilmentUiModel(
    val label: String,
) {
    PARALYSIS("Paralysis"),
    SLEEP("Sleep"),
    FREEZE("Freeze"),
    BURN("Burn"),
    POISON("Poison"),
    CONFUSION("Confusion"),
    INFATUATION("Infatuation"),
    TRAP("Trapped"),
    NIGHTMARE("Nightmare"),
    TORMENT("Torment"),
    DISABLE("Disable"),
    YAWN("Drowsiness"),
    HEAL_BLOCK("Heal Block"),
    NO_TYPE_IMMUNITY("Grounded"),
    LEECH_SEED("Leech Seed"),
    EMBARGO("Embargo"),
    PERISH_SONG("Perish Song"),
    INGRAIN("Ingrain"),
    SILENCE("Silence"),
    TAR_SHOT("Tar Shot"),
    PROTECT("Protection"),
    UNKNOWN("A status condition"),
}
