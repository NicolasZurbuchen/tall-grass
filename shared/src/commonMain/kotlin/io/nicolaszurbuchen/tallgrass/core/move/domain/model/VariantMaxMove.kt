package io.nicolaszurbuchen.tallgrass.core.move.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One row of what a Gigantamax form actually has in front of it: a Max Move, and the moves it learnt
 * that become it.
 *
 * **A Dynamaxed Pokemon does not have its own moves any more.** Every damaging move becomes the Max
 * Move of its type, every status move becomes Max Guard, and on a Gigantamax form the moves matching
 * its signature type become its G-Max Move instead. Sixty-odd moves collapse into ten or so, which
 * is why [sources] is here: without it the list is a set of names with no visible reason to be short.
 *
 * [power] is the best this Pokemon can reach with that Max Move, which is the strongest of its
 * [sources]. [damageClass] is that same move's, because a Max Move takes its category from whatever
 * it replaced — so a Pokemon with both a physical and a special Fire move is shown the one that hits
 * hardest rather than two rows that differ in a field nothing else on the row reflects.
 *
 * Both are null and empty respectively for nothing: a row exists only because a move became it.
 */
data class VariantMaxMove(
    val slug: String,
    val name: String,
    val type: PokemonType,
    val damageClass: DamageClass,
    val power: Int?,
    val isSignature: Boolean,
    val sources: List<VariantMove>,
)
