package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoveCategoryUiMapperTest {
    @Test
    fun toUiModel_mapsAllFourteenToTheirOwnLabels() {
        val labels = MoveCategory.entries.map { it.toUiModel() }

        assertEquals(14, MoveCategory.entries.size)
        assertEquals(MoveCategory.entries.size, labels.distinct().size)
        assertTrue(labels.all { it.label.isNotBlank() })
    }

    @Test
    fun toUiModel_saysWhatTheCategoryMeansRatherThanRepeatingItsKey() {
        assertEquals("Raises or lowers stats", MoveCategory.NET_GOOD_STATS.toUiModel().label)
        assertEquals("One-hit knockout", MoveCategory.OHKO.toUiModel().label)
        assertEquals("One of a kind", MoveCategory.UNIQUE.toUiModel().label)
    }
}
