package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel

import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_tab_about
import tallgrass.shared.generated.resources.move_detail_tab_learners

/**
 * The two tabs this screen has.
 *
 * The split is between what the move *is* and who can *use* it, which is also the split between what
 * one read gets and what a second read costs: the learners are 1,213 rows for Rest and nobody who
 * stays on the first tab pays for them.
 */
enum class MoveDetailTabUiModel(
    val label: UiText,
) {
    DETAILS(UiText.Resource(Res.string.move_detail_tab_about)),
    LEARNERS(UiText.Resource(Res.string.move_detail_tab_learners)),
}
