package io.nicolaszurbuchen.tallgrass.core.move.domain.model

/**
 * The mechanical detail behind a move's prose: how many times it hits, what it inflicts, how much it
 * drains.
 *
 * **Every number here is absent rather than zero when there is nothing to say**, which is what lets a
 * screen draw the fields it finds and nothing else.
 *
 * [ailmentChance] is the one that would be a bug if it were read literally: a null beside a non-null
 * [ailment] means *always*, and it is null on the thirty-six moves whose ailment is certain. Thunder
 * Wave does not paralyse 0% of the time. [statChance] says the same about [MoveDetail.statChanges].
 *
 * [drain] and [healing] are signed and the sign is the meaning: drain is a share of the damage dealt
 * and goes negative for recoil, healing is a share of the user's own maximum HP and goes negative
 * for the two moves that cost HP to use.
 */
data class MoveMeta(
    val category: MoveCategory,
    val ailment: MoveAilment?,
    val ailmentChance: Int?,
    val minHits: Int?,
    val maxHits: Int?,
    val minTurns: Int?,
    val maxTurns: Int?,
    val drain: Int?,
    val healing: Int?,
    val critRate: Int?,
    val flinchChance: Int?,
    val statChance: Int?,
)
