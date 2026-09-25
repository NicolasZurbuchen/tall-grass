package io.nicolaszurbuchen.tallgrass.core.ability.domain.model

/**
 * One ability a Pokemon has, as its own detail lists it.
 *
 * The mirror of [AbilityHolder], which is the same relationship read the other way. This side is
 * thin for the same reason [Ability] is: the row draws a name and a line, and the paragraph behind
 * it is one tap away.
 *
 * [isHidden] is the third slot -- the ability a Pokemon cannot be caught with ordinarily -- and it is
 * worth saying on the row because it changes what a reader has to do to get it.
 */
data class VariantAbility(
    val slug: String,
    val name: String,
    val shortEffect: String,
    val isHidden: Boolean,
)
