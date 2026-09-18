package io.nicolaszurbuchen.tallgrass.core.type.domain.model

/**
 * One non-neutral cell of the type chart, read against a target type the caller already knows.
 *
 * [factorPercent] is upstream's encoding — 0, 50, 200 — and only non-neutral pairs are stored, so an
 * attacking type with no cell deals normal damage.
 */
data class TypeEfficacy(
    val damageType: PokemonType,
    val factorPercent: Int,
)
