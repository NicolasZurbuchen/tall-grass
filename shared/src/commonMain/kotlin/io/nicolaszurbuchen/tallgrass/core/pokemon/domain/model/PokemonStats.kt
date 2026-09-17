package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * One variant's six base stats.
 *
 * A record rather than a list of name/value pairs: the six are fixed, they are always all present,
 * and every screen that draws them draws them in this order.
 */
data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
) {
    val total: Int
        get() = hp + attack + defense + specialAttack + specialDefense + speed
}
