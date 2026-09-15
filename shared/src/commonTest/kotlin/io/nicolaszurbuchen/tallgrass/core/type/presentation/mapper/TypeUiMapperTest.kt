package io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeUiMapperTest {
    @Test
    fun toUiModel_everyType_mapsToADistinctUiModel() {
        // The point of the assertion is the second half: two types sharing a colour would make the
        // grid quietly ambiguous rather than fail anything.
        val mapped = PokemonType.entries.map { it.toUiModel() }

        assertEquals(PokemonType.entries.size, mapped.size)
        assertEquals(mapped.size, mapped.toSet().size)
        assertEquals(mapped.size, mapped.map { it.color }.toSet().size)
    }

    @Test
    fun toUiModel_mapsByMeaningRatherThanOrdinal() {
        assertEquals(TypeUiModel.FIRE, PokemonType.FIRE.toUiModel())
        assertEquals(TypeUiModel.FAIRY, PokemonType.FAIRY.toUiModel())
        assertEquals(TypeUiModel.WATER, PokemonType.WATER.toUiModel())
    }
}
