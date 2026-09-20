package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable

/**
 * Everything on this screen that had to be read before it could be drawn.
 *
 * The name, the types, the artwork, its shared-element key and the tint are deliberately not here:
 * they are known from the destination the moment the screen opens, and putting them behind the same
 * nullable field would mean the hero could not draw — or match a card — until the database answered.
 * See `HeroHandoff`.
 *
 * [forms] is empty for a species with one form, which is most of them.
 */
@Immutable
data class DetailContentUiModel(
    val numberText: String,
    val genusText: String,
    val forms: List<FormPillUiModel>,
    val activeFormSlug: String,
    val tab: DetailTabUiModel,
    val about: AboutUiModel,
    val stats: StatsUiModel,
)
