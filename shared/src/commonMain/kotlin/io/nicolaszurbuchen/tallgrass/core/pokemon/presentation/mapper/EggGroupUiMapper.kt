package io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.uimodel.EggGroupUiModel

/**
 * Exhaustive by construction: both enums list the same fifteen members, so a sixteenth group breaks
 * this at compile time rather than leaving a breeding block with a blank in it.
 */
fun EggGroup.toUiModel(): EggGroupUiModel =
    when (this) {
        EggGroup.MONSTER -> EggGroupUiModel.MONSTER
        EggGroup.WATER_1 -> EggGroupUiModel.WATER_1
        EggGroup.BUG -> EggGroupUiModel.BUG
        EggGroup.FLYING -> EggGroupUiModel.FLYING
        EggGroup.FIELD -> EggGroupUiModel.FIELD
        EggGroup.FAIRY -> EggGroupUiModel.FAIRY
        EggGroup.GRASS -> EggGroupUiModel.GRASS
        EggGroup.HUMAN_LIKE -> EggGroupUiModel.HUMAN_LIKE
        EggGroup.WATER_3 -> EggGroupUiModel.WATER_3
        EggGroup.MINERAL -> EggGroupUiModel.MINERAL
        EggGroup.AMORPHOUS -> EggGroupUiModel.AMORPHOUS
        EggGroup.WATER_2 -> EggGroupUiModel.WATER_2
        EggGroup.DITTO -> EggGroupUiModel.DITTO
        EggGroup.DRAGON -> EggGroupUiModel.DRAGON
        EggGroup.UNDISCOVERED -> EggGroupUiModel.UNDISCOVERED
    }
