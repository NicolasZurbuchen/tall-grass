package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoveTargetUiMapperTest {
    @Test
    fun toUiModel_coversAllSixteenWithoutFallingThrough() {
        assertEquals(16, MoveTarget.entries.size)
        assertTrue(MoveTarget.entries.all { it.toUiModel().label.isNotBlank() })
    }

    @Test
    fun toUiModel_saysTheSameThingForTheTwoTargetsThatAreTheSameThing() {
        // Upstream separates "one target" from the Me First variant of it, and a reader choosing a
        // move does not care which. Deliberately not distinct, unlike every other enum mapper here.
        assertEquals(
            MoveTarget.SELECTED_POKEMON.toUiModel().label,
            MoveTarget.SELECTED_POKEMON_ME_FIRST.toUiModel().label,
        )
    }

    @Test
    fun toUiModel_readsAsAPhraseRatherThanAsUpstreamsKey() {
        assertEquals("Itself", MoveTarget.USER.toUiModel().label)
        assertEquals("All opponents", MoveTarget.ALL_OPPONENTS.toUiModel().label)
        assertEquals("The whole field", MoveTarget.ENTIRE_FIELD.toUiModel().label)
    }
}
