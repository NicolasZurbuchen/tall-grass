package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveUiMapperTest {
    @Test
    fun toUiModel_carriesEveryFieldTheCardDraws() {
        val card = MoveFixtures.flamethrower.toUiModel()

        assertEquals("flamethrower", card.slug)
        assertEquals("Flamethrower", card.name)
        assertEquals(TypeUiModel.FIRE, card.type)
        assertEquals(DamageClassUiModel.SPECIAL, card.damageClass)
        assertEquals("90", card.powerText)
    }

    @Test
    fun toUiModel_drawsAMoveWithNoPowerAsADashRatherThanAZero() {
        // 331 of the 919 are status moves. A zero would claim they hit for nothing, which is a
        // different and wrong statement -- see DECISIONS.md § A move's absent numbers are absent,
        // not zero.
        assertEquals("—", MoveFixtures.thunderWave.toUiModel().powerText)
    }

    @Test
    fun toUiModel_takesTheCardsColourFromTheMovesType() {
        assertEquals(TypeUiModel.ELECTRIC.color, MoveFixtures.thunderWave.toUiModel().type.color)
    }
}
