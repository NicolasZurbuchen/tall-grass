package io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel

/**
 * What kind of thing a move is, in upstream's own fourteen-way split.
 *
 * [SWAGGER] keeps its name because there is no shorter way to say what its four members do: raise a
 * stat and confuse in one move. [UNIQUE] is upstream's residue and says so rather than pretending to
 * be a category.
 */
enum class MoveCategoryUiModel(
    val label: String,
) {
    DAMAGE("Damage"),
    AILMENT("Inflicts a condition"),
    NET_GOOD_STATS("Raises or lowers stats"),
    HEAL("Healing"),
    DAMAGE_AILMENT("Damage and a condition"),
    SWAGGER("Raises a stat and confuses"),
    DAMAGE_LOWER("Damage and lowers a stat"),
    DAMAGE_RAISE("Damage and raises a stat"),
    DAMAGE_HEAL("Damage and healing"),
    OHKO("One-hit knockout"),
    WHOLE_FIELD_EFFECT("Affects the whole field"),
    FIELD_EFFECT("Affects one side"),
    FORCE_SWITCH("Forces a switch"),
    UNIQUE("One of a kind"),
}
