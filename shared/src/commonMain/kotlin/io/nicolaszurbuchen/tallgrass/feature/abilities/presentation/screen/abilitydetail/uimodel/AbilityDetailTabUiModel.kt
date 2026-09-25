package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.uimodel

import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.ability_detail_tab_about
import tallgrass.shared.generated.resources.ability_detail_tab_holders

/**
 * The two tabs this screen has.
 *
 * The same split a move's detail makes: what the thing *is*, and who *has* it. Two questions with two
 * shapes -- a paragraph and a grid of pictures -- which is what makes them tabs rather than sections.
 */
enum class AbilityDetailTabUiModel(
    val label: UiText,
) {
    DETAILS(UiText.Resource(Res.string.ability_detail_tab_about)),
    HOLDERS(UiText.Resource(Res.string.ability_detail_tab_holders)),
}
