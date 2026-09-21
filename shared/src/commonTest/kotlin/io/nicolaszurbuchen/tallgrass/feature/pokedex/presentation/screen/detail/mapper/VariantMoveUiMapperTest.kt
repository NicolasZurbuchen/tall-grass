package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_move_level
import kotlin.test.Test
import kotlin.test.assertEquals

class VariantMoveUiMapperTest {
    @Test
    fun toUiModel_carriesWhatTheRowDraws() {
        val row = MoveFixtures.charizardFlamethrower.toUiModel()

        assertEquals("flamethrower", row.slug)
        assertEquals("Flamethrower", row.name)
        assertEquals(TypeUiModel.FIRE, row.type)
        assertEquals(DamageClassUiModel.SPECIAL, row.damageClass)
        assertEquals("90", row.powerText)
    }

    @Test
    fun toUiModel_prefersTheLevelOverTheMethod() {
        // The same rule the Learned by tab on a move uses. The two lists are the same rows read from
        // opposite ends, and a reader who has seen one should not have to learn the other.
        val how = MoveFixtures.charizardFlamethrower.toUiModel().howText as UiText.Resource

        assertEquals(Res.string.pokedex_detail_move_level, how.id)
        assertEquals(listOf(46), how.args)
    }

    @Test
    fun toUiModel_fallsBackToTheMethodWhereThereIsNoLevel() {
        val tm = MoveFixtures.charizardFlamethrower.copy(method = LearnMethod.MACHINE, level = null)

        assertEquals(UiText.Raw("TM"), tm.toUiModel().howText)
    }

    @Test
    fun toUiModel_drawsAStatusMovesPowerAsADash() {
        val status = MoveFixtures.charizardFlamethrower.copy(power = null)

        assertEquals("—", status.toUiModel().powerText)
    }
}
