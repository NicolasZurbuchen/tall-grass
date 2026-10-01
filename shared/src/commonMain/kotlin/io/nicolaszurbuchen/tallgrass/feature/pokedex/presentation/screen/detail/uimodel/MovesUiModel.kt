package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable

/**
 * The Moves tab: what this form can do, which is its abilities and then everything it learns.
 *
 * A model of its own, like the other three tabs have, rather than two loose lists on
 * [DetailContentUiModel]. The tab reads for itself when it is first opened for a form, so unlike
 * About and Stats it has a state the others do not: not read yet.
 *
 * [isLoading] is what tells that apart from an answer. Both lists fill together in a single pass, so
 * one flag covers the pair.
 */
@Immutable
data class MovesUiModel(
    val isLoading: Boolean,
    val abilities: List<VariantAbilityUiModel>,
    // Named for what it holds rather than for the tab, the way [StatsUiModel] names its bars: a
    // field called `moves` on a model called `MovesUiModel` reads as itself at every call site.
    //
    // Empty for the Arceus and Silvally type forms, which have no rows of their own upstream, and
    // empty for nothing else once the read has landed.
    val learned: List<VariantMoveUiModel>,
    // What [learned] becomes when the Pokemon Gigantamaxes, and empty for every form that cannot.
    // Non-empty is the test the tab draws on: a Gigantamax Pokemon does not use its own moves, so
    // where there are Max Moves they are the list rather than a second one beside it.
    val maxMoves: List<VariantMaxMoveUiModel>,
)
