package io.nicolaszurbuchen.tallgrass.core.move.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One move, whole.
 *
 * The nullable fields are absent rather than defaulted, and the counts say which absences are
 * ordinary: 331 moves have no [power], 285 no [accuracy], and 93 Generation VIII and IX moves no
 * [shortEffect] or [effect] because upstream has not written them. [pp] is the counter-example --
 * every move has one, so a null there is a read that went wrong.
 *
 * [meta] is null for 92 recent moves, very nearly the same set. [statChanges] is beside it rather
 * than inside it because fifteen moves have stat changes and no meta at all -- see
 * `DECISIONS.md § A move's mechanical detail is null where there is nothing to say`.
 */
data class MoveDetail(
    val slug: String,
    val name: String,
    val generation: Int,
    val type: PokemonType,
    val damageClass: DamageClass,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val priority: Int,
    val target: MoveTarget,
    val shortEffect: String?,
    val effect: String?,
    val meta: MoveMeta?,
    val statChanges: List<MoveStatChange>,
)
