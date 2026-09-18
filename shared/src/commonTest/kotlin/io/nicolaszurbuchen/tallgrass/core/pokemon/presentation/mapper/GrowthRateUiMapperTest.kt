package io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import kotlin.test.Test
import kotlin.test.assertEquals

class GrowthRateUiMapperTest {
    @Test
    fun toUiModel_mapsEveryRateToItsOwnLabel() {
        val labels = GrowthRate.entries.map { it.toUiModel() }

        assertEquals(GrowthRate.entries.size, labels.distinct().size)
    }

    @Test
    fun toUiModel_usesTheNameTheGamesGiveTheTwoOddCurves() {
        // Upstream calls these slow-then-very-fast and fast-then-very-slow, which no player would.
        assertEquals("Erratic", GrowthRate.ERRATIC.toUiModel().label)
        assertEquals("Fluctuating", GrowthRate.FLUCTUATING.toUiModel().label)
    }
}
