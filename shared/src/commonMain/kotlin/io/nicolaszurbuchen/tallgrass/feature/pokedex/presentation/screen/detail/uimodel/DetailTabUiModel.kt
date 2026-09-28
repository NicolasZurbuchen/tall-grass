package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_tab_about
import tallgrass.shared.generated.resources.pokedex_detail_tab_location
import tallgrass.shared.generated.resources.pokedex_detail_tab_moves
import tallgrass.shared.generated.resources.pokedex_detail_tab_stats

/**
 * The four tabs this screen has.
 *
 * Location is last because it is the only one asking about the world rather than about the Pokemon,
 * and because it reads again whenever the form switcher moves: Alolan Vulpix is not found where
 * Vulpix is.
 *
 * The name stays "Location" rather than "Encounters" or "Availability". #9 weighed all three:
 * Encounters is less accurate now that gift and trade appear, Availability is precise and reads
 * corporate, and Location is the convention every dex app uses -- the rows genuinely are places.
 */
enum class DetailTabUiModel(
    val label: UiText,
) {
    ABOUT(UiText.Resource(Res.string.pokedex_detail_tab_about)),
    STATS(UiText.Resource(Res.string.pokedex_detail_tab_stats)),
    MOVES(UiText.Resource(Res.string.pokedex_detail_tab_moves)),
    LOCATION(UiText.Resource(Res.string.pokedex_detail_tab_location)),
}
