package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

@Immutable
data class AbilitiesUiModel(
    val isLoading: Boolean,
    val abilities: List<AbilityUiModel>,
    val error: AppErrorUiModel?,
)

/**
 * One card in the abilities list.
 *
 * [initial] is the first letter of the name, drawn in the card's tile. The list is alphabetical and
 * 314 long, so the letter is what tells a reader scrolling past where they are -- which is the job
 * the design's tinted icon tile was doing with an icon nobody has drawn.
 *
 * [generationText] is the only other fact a card carries. Upstream has no category for abilities and
 * three attempts at inventing one were rejected, so this stands in until #65 answers it.
 */
@Immutable
data class AbilityUiModel(
    val slug: String,
    val name: String,
    val initial: String,
    val generationText: UiText,
    val shortEffect: String,
)
