package io.nicolaszurbuchen.tallgrass.core.type.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup
import io.nicolaszurbuchen.tallgrass.core.type.domain.repository.TypeRepository

class GetTypeMatchupsUseCase(
    private val repository: TypeRepository,
) {
    /**
     * What a defender of these types takes from each of the eighteen, worst first.
     *
     * Neutral matchups are absent rather than listed at 100: there are usually a dozen of them and
     * they say nothing, which is also why upstream does not store them.
     */
    suspend operator fun invoke(
        primaryType: PokemonType,
        secondaryType: PokemonType?,
    ): List<TypeMatchup> {
        val cells =
            repository.efficaciesAgainst(primaryType) +
                secondaryType?.let { repository.efficaciesAgainst(it) }.orEmpty()

        return PokemonType.entries
            .mapNotNull { attackingType ->
                // Percentages stay integers the whole way. 200% into 50% is exactly normal damage,
                // and asking that of two rounded floats is the one comparison this must not get
                // wrong -- nor may the label "x2" come out of a float that is nearly two.
                val percent =
                    cells
                        .filter { it.damageType == attackingType }
                        .fold(NEUTRAL_PERCENT) { acc, cell -> acc * cell.factorPercent / NEUTRAL_PERCENT }

                if (percent == NEUTRAL_PERCENT) null else TypeMatchup(attackingType, percent)
            }
            // Stable, so types that share a factor stay in the chart's own order.
            .sortedByDescending { it.factorPercent }
    }
}

private const val NEUTRAL_PERCENT = 100
