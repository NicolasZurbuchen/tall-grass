package io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel

import org.jetbrains.compose.resources.StringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.console_ds
import tallgrass.shared.generated.resources.console_game_cube
import tallgrass.shared.generated.resources.console_gb_gbc
import tallgrass.shared.generated.resources.console_gba
import tallgrass.shared.generated.resources.console_switch
import tallgrass.shared.generated.resources.console_three_ds

/**
 * The label on a row of the availability grid.
 *
 * The rendering half of `Console`, which is the domain's. The declaration order is the order the rows
 * are drawn -- newest machine first, because the row a reader is most likely to be holding belongs at
 * the top.
 */
enum class ConsoleUiModel(
    val label: StringResource,
) {
    SWITCH(Res.string.console_switch),
    THREE_DS(Res.string.console_three_ds),
    DS(Res.string.console_ds),
    GAME_CUBE(Res.string.console_game_cube),
    GBA(Res.string.console_gba),
    GB_GBC(Res.string.console_gb_gbc),
}
