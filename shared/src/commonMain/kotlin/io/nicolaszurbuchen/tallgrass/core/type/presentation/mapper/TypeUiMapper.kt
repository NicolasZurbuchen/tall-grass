package io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel

/**
 * Exhaustive by construction: both enums list the same eighteen members, so a nineteenth type breaks
 * this at compile time rather than leaving a card with no colour.
 */
fun PokemonType.toUiModel(): TypeUiModel =
    when (this) {
        PokemonType.NORMAL -> TypeUiModel.NORMAL
        PokemonType.FIRE -> TypeUiModel.FIRE
        PokemonType.WATER -> TypeUiModel.WATER
        PokemonType.ELECTRIC -> TypeUiModel.ELECTRIC
        PokemonType.GRASS -> TypeUiModel.GRASS
        PokemonType.ICE -> TypeUiModel.ICE
        PokemonType.FIGHTING -> TypeUiModel.FIGHTING
        PokemonType.POISON -> TypeUiModel.POISON
        PokemonType.GROUND -> TypeUiModel.GROUND
        PokemonType.FLYING -> TypeUiModel.FLYING
        PokemonType.PSYCHIC -> TypeUiModel.PSYCHIC
        PokemonType.BUG -> TypeUiModel.BUG
        PokemonType.ROCK -> TypeUiModel.ROCK
        PokemonType.GHOST -> TypeUiModel.GHOST
        PokemonType.DRAGON -> TypeUiModel.DRAGON
        PokemonType.DARK -> TypeUiModel.DARK
        PokemonType.STEEL -> TypeUiModel.STEEL
        PokemonType.FAIRY -> TypeUiModel.FAIRY
    }
