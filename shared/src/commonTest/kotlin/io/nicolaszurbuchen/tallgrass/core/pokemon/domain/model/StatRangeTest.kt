package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class StatRangeTest {
    /**
     * Charizard's six, checked against the figures every other Pokedex prints.
     *
     * The whole value of these numbers is that a reader can look them up somewhere else and find the
     * same pair, so the test is the lookup rather than a restatement of the formula.
     */
    @Test
    fun ranges_matchThePublishedFiguresForALevelHundredCharizard() {
        assertEquals(StatRange(266, 360), StatRange.ofHp(78))
        assertEquals(StatRange(155, 293), StatRange.of(84))
        assertEquals(StatRange(144, 280), StatRange.of(78))
        assertEquals(StatRange(200, 348), StatRange.of(109))
        assertEquals(StatRange(157, 295), StatRange.of(85))
        assertEquals(StatRange(184, 328), StatRange.of(100))
    }

    @Test
    fun hp_isNarrowerThanEveryOtherStatBecauseNoNatureTouchesIt() {
        // 94 exactly, which is the 31 IVs and the quarter of 252 EVs and nothing else. Every other
        // stat's band is that plus a fifth of the stat itself.
        val hp = StatRange.ofHp(78)
        val defense = StatRange.of(78)

        assertEquals(94, hp.max - hp.min)
        assertEquals(136, defense.max - defense.min)
    }

    @Test
    fun shedinja_hasOneHitPointWhateverIsDoneToIt() {
        // The games hard-code it and so does this. The formula would say 112 to 206, and a Pokedex
        // that printed either would be wrong in the one way a reader is guaranteed to notice.
        assertEquals(StatRange(1, 1), StatRange.ofHp(1))
    }

    @Test
    fun aNature_floorsRatherThanRounds() {
        // Attack 84 lands on 155.7 at the bottom and 293.7 at the top. Rounding would print 156 and
        // 294, and the second is a figure no Pokemon can reach -- which is the failure worth naming,
        // because it is the end a reader building a team would check.
        assertEquals(StatRange(155, 293), StatRange.of(84))
    }
}
