package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantAbilityUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_ability_hidden

/**
 * The same lettered tile the abilities list draws, so a row here and a card there read as the same
 * thing seen twice. The initial is taken from the name for the same reason it is there.
 */
fun VariantAbility.toUiModel(): VariantAbilityUiModel =
    VariantAbilityUiModel(
        slug = slug,
        name = name,
        initial = name.take(1).uppercase(),
        shortEffect = shortEffect,
        hiddenText = UiText.Resource(Res.string.pokedex_detail_ability_hidden).takeIf { isHidden },
    )
