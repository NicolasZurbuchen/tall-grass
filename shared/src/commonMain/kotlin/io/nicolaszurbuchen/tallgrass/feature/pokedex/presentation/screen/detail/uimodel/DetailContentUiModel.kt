package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable

/**
 * Everything on this screen that had to be read before it could be drawn.
 *
 * The name, the number, the types, the artwork and the tint are deliberately not here: all of them
 * are known from the card the carousel is on, and putting them behind the same nullable field would
 * mean the hero could not draw — or match a card — until the database answered. See `HeroHandoff`.
 *
 * [forms] is empty for a species with one form, which is most of them.
 */
@Immutable
data class DetailContentUiModel(
    val genusText: String,
    val forms: List<FormPillUiModel>,
    val activeFormSlug: String,
    val tab: DetailTabUiModel,
    val about: AboutUiModel,
    val stats: StatsUiModel,
    // Its own model rather than loose lists here, because the tab reads for itself and so has a
    // state About and Stats do not: not read yet.
    val moves: MovesUiModel,
    // Its own model for the same reason and one more: the tab has a selection of its own, and which
    // game is open survives switching to Stats and back.
    val location: LocationUiModel,
)
