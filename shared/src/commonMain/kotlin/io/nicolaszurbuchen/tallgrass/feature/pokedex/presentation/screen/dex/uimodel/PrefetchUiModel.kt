package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.uimodel

import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * The artwork run, as the one line about it the dex shows.
 *
 * [fraction] is null when there is no bar to draw — the run has stopped and the line is now a
 * sentence rather than progress.
 */
data class PrefetchUiModel(
    val message: UiText,
    val fraction: Float?,
)
