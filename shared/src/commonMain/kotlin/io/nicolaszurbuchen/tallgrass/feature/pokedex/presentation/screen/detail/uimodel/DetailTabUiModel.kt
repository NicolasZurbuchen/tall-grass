package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_tab_about
import tallgrass.shared.generated.resources.pokedex_detail_tab_moves
import tallgrass.shared.generated.resources.pokedex_detail_tab_stats

/**
 * The three tabs this screen has. Location joins them once the dataset it needs exists, which is why
 * the row is a list rather than hard-coded labels.
 */
enum class DetailTabUiModel(
    val label: UiText,
) {
    ABOUT(UiText.Resource(Res.string.pokedex_detail_tab_about)),
    STATS(UiText.Resource(Res.string.pokedex_detail_tab_stats)),
    MOVES(UiText.Resource(Res.string.pokedex_detail_tab_moves)),
}
