package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * The effort values defeating one form awards, one figure per stat.
 *
 * The same six fields in the same order as [PokemonStats], and a different fact: those are what the
 * Pokemon has, these are what beating it gives the Pokemon that beat it. Nearly all of them are
 * zero — 1,214 of the 1,336 costed forms award against a single stat and only twelve spread over
 * three — so a reader of this record is looking for the one or two that are not.
 *
 * **There is no all-zero instance.** Upstream has not costed 49 forms, all of them Legends Z-A
 * Megas, and they are absent rather than zeroed: see `PokemonVariant.evYield`.
 */
data class EvYield(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
)
