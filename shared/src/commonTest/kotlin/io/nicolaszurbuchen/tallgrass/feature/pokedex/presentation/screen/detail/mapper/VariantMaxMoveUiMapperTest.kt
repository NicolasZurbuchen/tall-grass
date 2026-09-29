package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMaxMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VariantMaxMoveUiMapperTest {
    private fun source(name: String) =
        VariantMove(
            slug = name.lowercase(),
            name = name,
            type = PokemonType.FIRE,
            damageClass = DamageClass.SPECIAL,
            power = 90,
            maxPower = 130,
            method = LearnMethod.LEVEL_UP,
            level = 1,
        )

    private fun wildfire(
        power: Int? = 130,
        sources: List<VariantMove> = listOf(source("Flamethrower")),
    ) = VariantMaxMove(
        slug = "g-max-wildfire",
        name = "G-Max Wildfire",
        type = PokemonType.FIRE,
        damageClass = DamageClass.SPECIAL,
        power = power,
        isSignature = true,
        sources = sources,
    )

    @Test
    fun toUiModel_carriesTheMoveAndItsColour() {
        val ui = wildfire().toUiModel()

        assertEquals("G-Max Wildfire", ui.name)
        assertEquals(TypeUiModel.FIRE, ui.type)
        assertEquals(DamageClassUiModel.SPECIAL, ui.damageClass)
        assertEquals("130", ui.powerText)
        assertTrue(ui.isSignature)
    }

    @Test
    fun toUiModel_namesEverySourceRatherThanCountingThem() {
        // "From 3 moves" answers a question nobody asked. The names are what let a reader see that
        // the move they came looking for is still there, under another name.
        val sources = listOf(source("Fire Blast"), source("Flamethrower"), source("Heat Wave"))

        assertEquals(
            UiText.Raw("Fire Blast · Flamethrower · Heat Wave"),
            wildfire(sources = sources).toUiModel().sourcesText,
        )
    }

    @Test
    fun toUiModel_drawsMaxGuardsAbsentPowerAsADashRatherThanAZero() {
        // Max Guard inflicts nothing, which is the point of it, and a zero would read as a move that
        // hits for nothing rather than one that does not hit.
        assertEquals("—", wildfire(power = null).toUiModel().powerText)
    }
}
