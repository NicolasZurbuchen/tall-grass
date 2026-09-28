package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Console
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.GameVersion
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityCellUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityRowUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.ConsoleUiModel

/**
 * The grid, built from whatever games are in scope.
 *
 * **Rows are data rather than a fixed five.** A console with no games here gets no row at all, which
 * is what gives Kanto twelve cells and no 3DS row -- Kanto has no 3DS game -- and gives Orre a
 * GameCube row that appears nowhere else. #24 is explicit that this is derived and not hardcoded.
 *
 * [encountered] decides the one binary a cell's colour carries, and the cells that are not in it stay
 * tappable: that is how a negative gets confirmed, and how the two empty states get told apart at the
 * moment somebody asks.
 */
fun List<GameVersion>.toAvailabilityGridUiModel(encountered: Set<String>): AvailabilityGridUiModel =
    AvailabilityGridUiModel(
        rows =
            groupBy { it.console }
                .entries
                // The domain enum's own order, newest machine first, rather than the order the games
                // happened to arrive in. Sorted as a list because `toSortedMap` is JVM-only and this
                // is common code.
                .sortedBy { it.key.ordinal }
                .map { (console, versions) ->
                    AvailabilityRowUiModel(
                        console = console.toUiModel(),
                        cells =
                            versions.map { version ->
                                AvailabilityCellUiModel(
                                    slug = version.slug,
                                    name = version.name,
                                    code = version.code,
                                    isAvailable = version.slug in encountered,
                                )
                            },
                    )
                },
    )

/**
 * Exhaustive by construction, the same way the type mapper is: both enums list the same six members,
 * so a seventh console breaks this at compile time rather than leaving a row with no label.
 */
fun Console.toUiModel(): ConsoleUiModel =
    when (this) {
        Console.SWITCH -> ConsoleUiModel.SWITCH
        Console.THREE_DS -> ConsoleUiModel.THREE_DS
        Console.DS -> ConsoleUiModel.DS
        Console.GAME_CUBE -> ConsoleUiModel.GAME_CUBE
        Console.GBA -> ConsoleUiModel.GBA
        Console.GB_GBC -> ConsoleUiModel.GB_GBC
    }
