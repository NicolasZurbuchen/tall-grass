package io.nicolaszurbuchen.tallgrass.datagen

/**
 * The closed taxonomy a screen filters on, as opposed to the open `form` identifier it is derived
 * from. See DECISIONS.md § A form's kind is derived and stored, not computed at read time.
 *
 * [NONE] is every default variant. [ALTERNATE] is battle-relevant in some way the other members do
 * not name; [COSMETIC] differs from its base form in nothing this dataset models.
 */
enum class FormKind {
    NONE,
    REGIONAL,
    MEGA,
    GIGANTAMAX,
    BATTLE_ONLY,
    GENDER,
    TOTEM,
    ALTERNATE,
    COSMETIC,
}

/**
 * Classifies a form. First match wins.
 *
 * The order is load-bearing: Gigantamax and gender forms carry their base form's stats, types *and*
 * abilities exactly, so reaching the [differsFromBase] test would file all 34 Gigantamax forms as
 * cosmetic.
 *
 * [differsFromBase] must account for abilities. See DECISIONS.md § A form's kind is derived and
 * stored, not computed at read time.
 */
fun classifyForm(
    isDefault: Boolean,
    form: String,
    isMega: Boolean,
    isBattleOnly: Boolean,
    differsFromBase: Boolean,
): FormKind =
    when {
        isDefault -> FormKind.NONE
        isMega -> FormKind.MEGA
        form == "gmax" -> FormKind.GIGANTAMAX
        isRegionalForm(form, isBattleOnly) -> FormKind.REGIONAL
        form == "male" || form == "female" -> FormKind.GENDER
        form.startsWith("totem") -> FormKind.TOTEM
        isBattleOnly -> FormKind.BATTLE_ONLY
        differsFromBase -> FormKind.ALTERNATE
        else -> FormKind.COSMETIC
    }
