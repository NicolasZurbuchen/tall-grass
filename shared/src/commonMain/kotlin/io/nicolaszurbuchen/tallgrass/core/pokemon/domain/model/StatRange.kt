package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * What one base stat becomes on a level 100 Pokemon, at the two ends of what a trainer can do to it.
 *
 * A base stat on its own is a number with no unit: 84 Attack means something only next to another
 * Pokemon's 84. The range is what it turns into on a Pokemon somebody actually has, and it is wide
 * -- Charizard's Attack is anywhere from 155 to 293 depending on nothing but how it was raised.
 *
 * **Level 100 and nature included**, which is the pair every other Pokedex prints, so a reader
 * checking these against one will find the same figures. [min] is 0 IVs, 0 EVs and a hindering
 * nature; [max] is 31 IVs, 252 EVs and a beneficial one.
 *
 * HP takes neither nature nor the same formula, so its band is narrower than the others by exactly
 * the twenty per cent the nature would have moved.
 */
data class StatRange(
    val min: Int,
    val max: Int,
) {
    companion object {
        /**
         * HP, which is its own formula: no nature, and level added a second time.
         *
         * **Shedinja is the exception the games hard-code**, and it is hard-coded here too: its HP is
         * 1 whatever is done to it, where the formula would say 112 to 206. Keyed on a base of 1
         * because it is the only Pokemon that has one -- `DatasetTest` asserts that, so this stops
         * being safe loudly rather than quietly.
         */
        fun ofHp(base: Int): StatRange =
            if (base == SHEDINJA_HP) {
                StatRange(SHEDINJA_HP, SHEDINJA_HP)
            } else {
                StatRange(min = 2 * base + HP_FLOOR, max = 2 * base + HP_FLOOR + BEST_EFFORT)
            }

        /**
         * Every stat but HP.
         *
         * The nature multipliers are integer arithmetic rather than 0.9f and 1.1f: the game floors
         * the result, and a float that lands a hair under a whole number would floor to one less.
         */
        fun of(base: Int): StatRange =
            StatRange(
                min = (2 * base + OTHER_FLOOR) * HINDERING_NUMERATOR / NATURE_DENOMINATOR,
                max = (2 * base + OTHER_FLOOR + BEST_EFFORT) * BENEFICIAL_NUMERATOR / NATURE_DENOMINATOR,
            )
    }
}

// What 31 IVs and 252 EVs add: the IVs whole, the EVs a quarter of themselves. The same 94 for every
// stat, which is why it is one constant rather than two.
private const val BEST_EFFORT = 31 + 252 / 4

// Level, then level again, then the ten every Pokemon gets. At level 100 the rest of the HP formula
// collapses to doubling the base, so this is the whole of what is added to it.
private const val HP_FLOOR = 100 + 10

// The five every other stat gets. There is no second level term and no ten.
private const val OTHER_FLOOR = 5

// x0.9 and x1.1, as tenths. A nature moves one stat up and another down, so the two ends of the band
// are never reachable by the same Pokemon at once.
private const val HINDERING_NUMERATOR = 9
private const val BENEFICIAL_NUMERATOR = 11
private const val NATURE_DENOMINATOR = 10

// Its HP is 1 and its base HP is 1, and no other Pokemon has either.
private const val SHEDINJA_HP = 1
