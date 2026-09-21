package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.MoveUiModel

/**
 * One card. Separate from `MovesUiMapper` so the list can be mapped without mapping the state around
 * it — see `MovesViewModel`.
 *
 * The name needs no work: upstream's `move_names` is already the label the games print, hyphens and
 * all, which is not true of every table in this dataset — an egg group arrives as `humanshape`.
 */
fun Move.toUiModel(): MoveUiModel =
    MoveUiModel(
        slug = slug,
        name = name,
        type = type.toUiModel(),
        damageClass = damageClass.toUiModel(),
        powerText = power?.toString() ?: NO_POWER,
    )

// An em dash rather than a zero. A status move has no power at all, and 0 would read as a move that
// hits for nothing — see DECISIONS.md § A move's absent numbers are absent, not zero.
private const val NO_POWER = "—"
