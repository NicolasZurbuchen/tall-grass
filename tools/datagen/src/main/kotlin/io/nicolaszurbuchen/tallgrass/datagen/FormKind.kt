package io.nicolaszurbuchen.tallgrass.datagen

/**
 * What kind of thing a form is.
 *
 * The closed companion to the raw `form` identifier, which is an open set: **164 distinct values of
 * which 134 are used exactly once**, so `alola` and `rock-star` sit at the same level and neither
 * can be filtered on. This is the field a screen groups by.
 *
 * Derived rather than stored upstream, which puts it in the same category as the ability categories
 * in issue #27: classified at generation time, committed, and reviewed in the JSON diff. A wrong
 * classification shows up in a pull request rather than on a device.
 */
enum class FormKind {
    /** The ordinary form. Every default variant, and nothing else. */
    NONE,
    REGIONAL,
    MEGA,
    GIGANTAMAX,
    BATTLE_ONLY,
    GENDER,
    TOTEM,

    /** Battle-relevant in some way the buckets above do not name — Rotom Wash, Deoxys Attack. */
    ALTERNATE,

    /** Differs from its base form in nothing this dataset models. Pikachu's costumes. */
    COSMETIC,
}

/**
 * Classifies a form. First match wins, and the order matters.
 *
 * [differsFromBase] must compare stats, types **and abilities**. Comparing only stats and types puts
 * eight real forms in [COSMETIC] — `greninja-battle-bond`, `rockruff-own-tempo`,
 * `toxtricity-low-key`, `zygarde-50-power-construct`, `basculin-blue-striped` and two Squawkabilly
 * plumages all differ by ability alone, and would otherwise be filed as costumes.
 *
 * The earlier buckets run first for a reason: Gigantamax and gender forms carry their base form's
 * stats, types *and* abilities exactly, so reaching the [differsFromBase] test would classify all 34
 * Gigantamax forms as cosmetic.
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
