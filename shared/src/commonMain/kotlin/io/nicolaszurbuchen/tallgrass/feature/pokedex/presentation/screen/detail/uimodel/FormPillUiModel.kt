package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/** One form in the switcher. [slug] is what selecting it selects. */
@Immutable
data class FormPillUiModel(
    val slug: String,
    val label: UiText,
)
