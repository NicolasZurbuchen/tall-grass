package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.mapper.toUiModel

/**
 * [abilities] is mapped from the State by default, which is what every caller but one wants.
 *
 * `AbilitiesViewModel` passes its own, on the same grounds as `MovesViewModel`: mapping the cards is
 * the expensive half of this function.
 */
fun AbilitiesState.toUiModel(abilities: List<AbilityUiModel> = this.abilities.map { it.toUiModel() }): AbilitiesUiModel =
    AbilitiesUiModel(
        isLoading = isLoading,
        abilities = abilities,
        error = error?.toUiModel(),
    )
