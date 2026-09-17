package io.nicolaszurbuchen.tallgrass.datagen

private val REGIONS = listOf("alola", "galar", "hisui", "paldea")

/**
 * Whether a form earns its own card in the dex grid.
 *
 * A prefix match on the region name alone gets two live cases wrong: `alola-cap` is Pikachu in a hat
 * rather than an Alolan Pikachu, and `galar-zen` is Zen Mode — a transformation *of* a regional form,
 * separated from `galar-standard` only by `is_battle_only`.
 */
fun isRegionalForm(
    formIdentifier: String,
    isBattleOnly: Boolean,
): Boolean {
    if (isBattleOnly) return false

    val region =
        REGIONS.firstOrNull { formIdentifier == it || formIdentifier.startsWith("$it-") }
            ?: return false

    val suffix = formIdentifier.removePrefix(region).removePrefix("-")
    return suffix.isEmpty() || suffix.endsWith("breed") || suffix == "standard"
}
