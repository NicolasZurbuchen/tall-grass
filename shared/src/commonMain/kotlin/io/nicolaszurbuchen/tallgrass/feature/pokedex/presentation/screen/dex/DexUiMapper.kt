package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.mapper.toUiModel

/**
 * [entries] is mapped from the State by default, which is what every caller but one wants.
 *
 * `DexViewModel` passes its own, because mapping a thousand cards is the expensive half of this
 * function and the prefetch produces states that change everything except them.
 */
fun DexState.toUiModel(entries: List<DexEntryUiModel> = this.entries.map { it.toUiModel() }): DexUiModel =
    DexUiModel(
        isLoading = isLoading,
        entries = entries,
        error = error?.toUiModel(),
        prefetch = prefetch?.toUiModel(),
    )
