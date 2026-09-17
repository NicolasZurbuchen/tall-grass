package io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.uimodel.EggGroupUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class EggGroupUiMapperTest {
    @Test
    fun toUiModel_mapsEveryGroupToItsOwnLabel() {
        val labels = EggGroup.entries.map { it.toUiModel() }

        assertEquals(EggGroup.entries.size, labels.distinct().size)
    }

    @Test
    fun toUiModel_rendersTheGroupsUpstreamNamesDifferently() {
        // The three the dataset spells nothing like the games do. Getting these wrong is invisible
        // in a diff and obvious to anyone who has bred a Pokemon.
        assertEquals(EggGroupUiModel.GRASS, EggGroup.GRASS.toUiModel())
        assertEquals("Grass", EggGroup.GRASS.toUiModel().label)
        assertEquals("Field", EggGroup.FIELD.toUiModel().label)
        assertEquals("Human-Like", EggGroup.HUMAN_LIKE.toUiModel().label)
    }
}
