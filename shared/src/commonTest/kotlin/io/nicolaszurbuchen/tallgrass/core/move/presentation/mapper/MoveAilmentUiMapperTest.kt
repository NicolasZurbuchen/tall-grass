package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveAilment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoveAilmentUiMapperTest {
    @Test
    fun toUiModel_mapsEveryAilmentToItsOwnLabel() {
        val labels = MoveAilment.entries.map { it.toUiModel() }

        assertEquals(MoveAilment.entries.size, labels.distinct().size)
        assertTrue(labels.all { it.label.isNotBlank() })
    }

    @Test
    fun toUiModel_saysWhatUnknownActuallyMeans() {
        // It is not a failed read. Four moves vary their ailment -- Tri Attack picks one of burn,
        // freeze and paralysis -- and upstream files that as -1.
        assertEquals("A status condition", MoveAilment.UNKNOWN.toUiModel().label)
    }
}
