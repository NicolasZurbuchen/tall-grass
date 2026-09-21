package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.BattleStat
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.BattleStatUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class BattleStatUiMapperTest {
    @Test
    fun toUiModel_mapsEveryStatToItsOwnLabel() {
        val labels = BattleStat.entries.map { it.toUiModel() }

        assertEquals(BattleStat.entries.size, labels.distinct().size)
    }

    @Test
    fun toUiModel_keepsTheDeclarationOrderTheStatBarsAreDrawnIn() {
        // The order is load-bearing rather than incidental: moveStatChange has no ordering column,
        // so the mapper sorts by this enum's ordinal. A reshuffle here reorders every move's
        // Mechanics block. See Move.sq.
        assertEquals(
            BattleStatUiModel.entries.map { it.name },
            BattleStat.entries.map { it.toUiModel().name },
        )
    }

    @Test
    fun toUiModel_abbreviatesTheTwoSpecialsTheWayAPokemonsStatTableDoes() {
        assertEquals("Sp. Atk", BattleStat.SPECIAL_ATTACK.toUiModel().label)
        assertEquals("Sp. Def", BattleStat.SPECIAL_DEFENSE.toUiModel().label)
    }
}
