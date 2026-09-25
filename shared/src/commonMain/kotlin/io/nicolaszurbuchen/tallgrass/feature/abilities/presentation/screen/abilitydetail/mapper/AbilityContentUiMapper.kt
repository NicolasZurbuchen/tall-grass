package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityContentUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.abilities_generation

/**
 * The generation reaches the hero's pill and nowhere else. It was also one of three figures at the
 * top of the Details tab, and those are gone -- see `AbilityContentUiModel`.
 */
fun AbilityDetail.toUiModel(): AbilityContentUiModel =
    AbilityContentUiModel(
        name = name,
        generationText = UiText.Resource(Res.string.abilities_generation, listOf(generation)),
        shortEffect = shortEffect,
        // Upstream repeats itself for 46 of the 314, and a section that repeats the line above it
        // reads as a rendering bug rather than as an ability with little to say.
        effect = effect.takeIf { it != shortEffect },
    )
