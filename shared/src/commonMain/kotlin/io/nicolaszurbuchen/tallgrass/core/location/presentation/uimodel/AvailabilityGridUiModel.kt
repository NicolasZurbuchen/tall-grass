package io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel

import androidx.compose.runtime.Immutable

/**
 * Every game as a cell, grouped into console rows.
 *
 * **One component, two directions.** A route asks which games have anything on it and a Pokemon asks
 * which games have it at all; the grid is the same either way, which is what #24 noticed when it
 * found the two screens were one component pointed opposite ways.
 *
 * [rows] holds only the consoles that have games in this context, which is what gives Kanto twelve
 * cells and no 3DS row at all -- Kanto has no 3DS game. Rows are data rather than a fixed five, and
 * Orre's GameCube row exists for the same reason.
 */
@Immutable
data class AvailabilityGridUiModel(
    val rows: List<AvailabilityRowUiModel>,
)

@Immutable
data class AvailabilityRowUiModel(
    val console: ConsoleUiModel,
    val cells: List<AvailabilityCellUiModel>,
)

/**
 * One game.
 *
 * [code] is unique only within its row, which is the whole job of the grouping: `Y` is Yellow on the
 * GB row and Pokemon Y on the 3DS row.
 *
 * [isAvailable] is the **only** thing the colour encodes. #9 prototyped and rejected three richer
 * schemes -- segmenting the chip by method, a primary method plus pips, and colour-by-method -- and
 * settled on one binary: a Pokemon obtainable six ways must not look different from one obtainable
 * once, because the cell answers "can I get it here" and nothing else.
 *
 * An unavailable cell stays tappable, which is how a negative gets confirmed at the moment somebody
 * asks -- and how the two empty states get told apart.
 */
@Immutable
data class AvailabilityCellUiModel(
    val slug: String,
    val name: String,
    val code: String,
    val isAvailable: Boolean,
)
