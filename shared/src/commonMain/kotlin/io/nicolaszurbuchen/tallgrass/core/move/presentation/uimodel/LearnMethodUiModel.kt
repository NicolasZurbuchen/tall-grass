package io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel

/**
 * How a Pokemon comes by a move, as a player would say it. Deliberately does not know about
 * `LearnMethod`; the domain crossing happens once, in `LearnMethodUiMapper`.
 *
 * [LEVEL_UP]'s label is what a card falls back to when there is no level to print, which is the 160
 * moves a Pokemon already knows rather than learns.
 */
enum class LearnMethodUiModel(
    val label: String,
) {
    LEVEL_UP("Level up"),
    MACHINE("TM"),
    EGG("Egg"),
    TUTOR("Tutor"),
}
