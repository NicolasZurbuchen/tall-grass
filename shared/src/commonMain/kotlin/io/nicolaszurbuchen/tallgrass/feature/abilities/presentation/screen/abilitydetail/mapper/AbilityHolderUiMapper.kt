package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel.AbilityHolderUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.ability_detail_hidden

/**
 * The card says "Hidden" or says nothing. There is no word for the ordinary case that is worth the
 * line -- "Normal" would be a label invented to fill a slot, and the reader already knows that a
 * Pokemon in this grid has the ability.
 */
fun AbilityHolder.toUiModel(): AbilityHolderUiModel =
    AbilityHolderUiModel(
        slug = variantSlug,
        name = name,
        artworkUrl = artworkUrl,
        tint = primaryType.toUiModel(),
        hiddenText = UiText.Resource(Res.string.ability_detail_hidden).takeIf { isHidden },
    )
