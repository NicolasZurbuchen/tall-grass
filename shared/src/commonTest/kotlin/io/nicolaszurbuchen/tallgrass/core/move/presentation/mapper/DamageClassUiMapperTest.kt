package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class DamageClassUiMapperTest {
    @Test
    fun toUiModel_mapsEachClassToItsOwnLabel() {
        val labels = DamageClass.entries.map { it.toUiModel() }

        assertEquals(DamageClass.entries.size, labels.distinct().size)
    }

    @Test
    fun toUiModel_namesTheThreeAsTheGamesDo() {
        assertEquals(DamageClassUiModel.PHYSICAL, DamageClass.PHYSICAL.toUiModel())
        assertEquals("Special", DamageClass.SPECIAL.toUiModel().label)
        assertEquals("Status", DamageClass.STATUS.toUiModel().label)
    }
}
