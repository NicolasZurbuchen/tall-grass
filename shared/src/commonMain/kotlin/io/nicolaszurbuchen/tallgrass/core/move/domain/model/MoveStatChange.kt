package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/** One stat a move moves, in stages. [stages] runs -2 to +3 and is never zero. */
data class MoveStatChange(
    val stat: BattleStat,
    val stages: Int,
)
