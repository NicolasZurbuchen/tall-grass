package io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.uimodel

/**
 * A breeding group as a player would name it. Deliberately does not know about `EggGroup`; the
 * domain crossing happens once, in `EggGroupUiMapper`.
 */
enum class EggGroupUiModel(
    val label: String,
) {
    MONSTER("Monster"),
    WATER_1("Water 1"),
    BUG("Bug"),
    FLYING("Flying"),
    FIELD("Field"),
    FAIRY("Fairy"),
    GRASS("Grass"),
    HUMAN_LIKE("Human-Like"),
    WATER_3("Water 3"),
    MINERAL("Mineral"),
    AMORPHOUS("Amorphous"),
    WATER_2("Water 2"),
    DITTO("Ditto"),
    DRAGON("Dragon"),
    UNDISCOVERED("Undiscovered"),
}
