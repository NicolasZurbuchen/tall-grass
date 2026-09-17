package io.nicolaszurbuchen.tallgrass.core.type.domain.model

/**
 * How much damage one defender takes from [attackingType], with both of its types accounted for.
 *
 * [factorPercent] is upstream's encoding, kept rather than converted to a multiplier: it is one of
 * 0, 25, 50, 200 and 400, and the label a screen draws for each is exact only while it is an
 * integer. A neutral matchup is not a matchup and is absent.
 */
data class TypeMatchup(
    val attackingType: PokemonType,
    val factorPercent: Int,
)
