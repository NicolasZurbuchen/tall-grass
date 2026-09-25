package io.nicolaszurbuchen.tallgrass.core.move.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One move a Pokemon learns, as its own detail screen lists it.
 *
 * The mirror of [MoveLearner], which is the same row read the other way: a move asking who learns it
 * against a Pokemon asking what it knows. Both are `moveLearner`, and they carry different things
 * because a list of Pokemon and a list of moves show different things.
 *
 * [level] is set only where [method] is [LearnMethod.LEVEL_UP], and not for all of those: 160
 * level-up moves are known without being taught. See `DECISIONS.md` on the learnset.
 */
data class VariantMove(
    val slug: String,
    val name: String,
    val type: PokemonType,
    val damageClass: DamageClass,
    val power: Int?,
    val method: LearnMethod,
    val level: Int?,
)
