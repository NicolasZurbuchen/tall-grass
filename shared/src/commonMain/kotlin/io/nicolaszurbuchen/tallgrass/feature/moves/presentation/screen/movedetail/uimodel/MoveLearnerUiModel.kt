package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One Pokemon in the Learned by tab.
 *
 * [howText] is the level where there is one and the method where there is not, which is the same slot
 * answering one question: how does this Pokemon get the move. "Lv 46" says more than "Level up" and
 * is available for 17,288 of the 62,777 rows; the rest say "TM", "Egg" or "Tutor".
 */
@Immutable
data class MoveLearnerUiModel(
    val slug: String,
    val name: String,
    val artworkUrl: String,
    val tint: TypeUiModel,
    val howText: UiText,
)
