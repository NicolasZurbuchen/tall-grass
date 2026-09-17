package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * One Pokemon, with every form it has.
 *
 * [variants] is never empty and is in the order the form switcher lists them. Arceus and Silvally
 * have eighteen each, which is the case any component drawing this has to survive.
 */
data class PokemonDetail(
    val species: PokemonSpecies,
    val variants: List<PokemonVariant>,
)
