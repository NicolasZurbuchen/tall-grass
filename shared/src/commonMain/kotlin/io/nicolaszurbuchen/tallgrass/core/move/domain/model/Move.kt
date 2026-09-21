package io.nicolaszurbuchen.tallgrass.core.move.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One row in the moves list.
 *
 * Deliberately thin: the list holds all 919 at once, and everything a card draws is here. The rest
 * of a move is [MoveDetail], read one at a time.
 *
 * [power] is null for the 331 status moves, which have none rather than zero.
 */
data class Move(
    val slug: String,
    val name: String,
    val type: PokemonType,
    val damageClass: DamageClass,
    val power: Int?,
)
