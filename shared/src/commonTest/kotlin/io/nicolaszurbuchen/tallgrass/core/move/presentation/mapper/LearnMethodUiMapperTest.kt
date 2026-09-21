package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.LearnMethodUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class LearnMethodUiMapperTest {
    @Test
    fun toUiModel_mapsEachMethodToItsOwnLabel() {
        val labels = LearnMethod.entries.map { it.toUiModel() }

        assertEquals(LearnMethod.entries.size, labels.distinct().size)
    }

    @Test
    fun toUiModel_saysTMRatherThanMachine() {
        // Upstream's word is "machine" and no player has ever called it that.
        assertEquals(LearnMethodUiModel.MACHINE, LearnMethod.MACHINE.toUiModel())
        assertEquals("TM", LearnMethod.MACHINE.toUiModel().label)
        assertEquals("Level up", LearnMethod.LEVEL_UP.toUiModel().label)
    }
}
