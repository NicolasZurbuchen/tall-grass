package io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.uimodel

/** A growth curve as the games name it. The domain crossing happens once, in `GrowthRateUiMapper`. */
enum class GrowthRateUiModel(
    val label: String,
) {
    SLOW("Slow"),
    MEDIUM_SLOW("Medium Slow"),
    MEDIUM_FAST("Medium Fast"),
    FAST("Fast"),
    ERRATIC("Erratic"),
    FLUCTUATING("Fluctuating"),
}
