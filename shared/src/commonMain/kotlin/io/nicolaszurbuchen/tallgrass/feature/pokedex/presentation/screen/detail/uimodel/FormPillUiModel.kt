package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/** One form in the switcher. [slug] is what selecting it selects. */
data class FormPillUiModel(
    val slug: String,
    val label: UiText,
)
