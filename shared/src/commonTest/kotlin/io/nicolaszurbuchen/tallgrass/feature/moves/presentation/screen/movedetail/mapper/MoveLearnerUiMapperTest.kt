package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_level
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveLearnerUiMapperTest {
    @Test
    fun toUiModel_carriesWhatTheCardDraws() {
        val card = MoveFixtures.charizardLearner.toUiModel()

        assertEquals("charizard", card.slug)
        assertEquals("Charizard", card.name)
        assertEquals(TypeUiModel.FIRE, card.tint, "the card takes its colour from the primary type")
    }

    @Test
    fun toUiModel_prefersTheLevelOverTheMethod() {
        // "Lv 46" already says the move is learned by levelling, so printing both would be a word
        // that adds nothing beside a number that says everything.
        val how = MoveFixtures.charizardLearner.toUiModel().howText as UiText.Resource

        assertEquals(Res.string.move_detail_level, how.id)
        assertEquals(listOf(46), how.args)
    }

    @Test
    fun toUiModel_fallsBackToTheMethodWhereThereIsNoLevel() {
        // Every TM, egg and tutor row, and the 160 level-up moves a Pokemon knows without being
        // taught. The method is the only answer available and it is a real one.
        val tm = MoveFixtures.charizardLearner.copy(method = LearnMethod.MACHINE, level = null)
        val innate = MoveFixtures.charizardLearner.copy(level = null)

        assertEquals(UiText.Raw("TM"), tm.toUiModel().howText)
        assertEquals(UiText.Raw("Level up"), innate.toUiModel().howText)
    }
}
