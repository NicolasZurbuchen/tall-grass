package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.detail_tab_about
import tallgrass.shared.generated.resources.detail_tab_stats

/**
 * The two tabs this screen has today. Location and Moves join them once the datasets they need
 * exist, which is why the row is a list rather than two hard-coded labels.
 */
enum class DetailTabUiModel(
    val label: UiText,
) {
    ABOUT(UiText.Resource(Res.string.detail_tab_about)),
    STATS(UiText.Resource(Res.string.detail_tab_stats)),
}
