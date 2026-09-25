package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityStatUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.abilities_generation
import tallgrass.shared.generated.resources.ability_detail_generation
import tallgrass.shared.generated.resources.ability_detail_hidden
import tallgrass.shared.generated.resources.ability_detail_pokemon

/**
 * The three figures are what places an ability rather than what it does: when it arrived, how many
 * Pokemon have it, and how many of those can only get it as a hidden ability.
 *
 * The third is the one worth a tile. A hidden ability cannot be caught in the ordinary way, so
 * "96 Pokemon, 4 of them hidden" and "96 Pokemon, all of them hidden" are different answers to the
 * question someone opened this screen with.
 *
 * [holders] rather than a count, because the hidden figure is a count of a property of the rows and
 * the caller has the rows. Passing two numbers in would move that decision to the call site.
 */
fun AbilityDetail.toUiModel(holders: List<AbilityHolder>): AbilityContentUiModel =
    AbilityContentUiModel(
        name = name,
        generationText = UiText.Resource(Res.string.abilities_generation, listOf(generation)),
        stats =
            listOf(
                AbilityStatUiModel(
                    label = UiText.Resource(Res.string.ability_detail_generation),
                    valueText = generation.toString(),
                ),
                AbilityStatUiModel(
                    label = UiText.Resource(Res.string.ability_detail_pokemon),
                    valueText = holders.size.toString(),
                ),
                AbilityStatUiModel(
                    label = UiText.Resource(Res.string.ability_detail_hidden),
                    valueText = holders.count { it.isHidden }.toString(),
                ),
            ),
        shortEffect = shortEffect,
        // Upstream repeats itself for 46 of the 314, and a section that repeats the line above it
        // reads as a rendering bug rather than as an ability with little to say.
        effect = effect.takeIf { it != shortEffect },
    )
