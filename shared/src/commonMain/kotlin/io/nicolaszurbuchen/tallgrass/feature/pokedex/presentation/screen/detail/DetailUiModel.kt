package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailHeroUiModel

/**
 * [name], [numberText], [types] and [tint] are never absent and never wait: they come from the card
 * the carousel is on, which is known before anything is read. That is what lets the header change
 * the instant a swipe crosses rather than a frame later, and it is also why the four of them are
 * outside [content].
 *
 * [heroes] always holds at least one card — the one the screen opened with — and grows to the list
 * behind it when that read lands. [activeIndex] is the one centred.
 */
@Immutable
data class DetailUiModel(
    val isLoading: Boolean,
    val error: AppErrorUiModel?,
    val name: String,
    val numberText: String,
    val types: List<TypeUiModel>,
    val tint: Color,
    val heroes: List<DetailHeroUiModel>,
    val activeIndex: Int,
    val content: DetailContentUiModel?,
)
