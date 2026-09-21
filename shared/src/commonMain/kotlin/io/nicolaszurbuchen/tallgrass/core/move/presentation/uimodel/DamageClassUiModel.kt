package io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel

/**
 * How a move deals damage, as a player would name it. Deliberately does not know about
 * `DamageClass`; the domain crossing happens once, in `DamageClassUiMapper`.
 */
enum class DamageClassUiModel(
    val label: String,
) {
    PHYSICAL("Physical"),
    SPECIAL("Special"),
    STATUS("Status"),
}
