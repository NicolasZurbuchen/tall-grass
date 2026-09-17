package io.nicolaszurbuchen.tallgrass.datagen

/**
 * The four regions that give a Pokemon a form of its own in the grid.
 *
 * Kantonian Vulpix and Alolan Vulpix both sit at #037 and both get a card, because a Pokemon from a
 * different region reads as a different Pokemon. A Mega does not: it is a battle state of the same
 * one. See DECISIONS.md and issue #5.
 */
private val REGIONS = listOf("alola", "galar", "hisui", "paldea")

/**
 * Whether a form earns its own card in the dex grid.
 *
 * A prefix match on the region name alone gets two cases wrong, and both are live in the data:
 *
 * - **`alola-cap`** is Pikachu wearing a hat from Alola. Cosmetic, and listing it would put a second
 *   Pikachu in the grid whose only difference is headwear.
 * - **`galar-zen`** is Galarian Darmanitan's Zen Mode -- a transformation *of* a regional form
 *   rather than one itself. `is_battle_only` is what separates it from `galar-standard`, which is
 *   the regional form and does get a card.
 *
 * So the suffix after the region has to be nothing at all, a Paldean Tauros breed, or Darmanitan's
 * `standard`. Anything else is a costume or a battle state.
 *
 * Getting this wrong is visible in the committed JSON diff rather than at runtime, which is the
 * reason that diff is reviewed.
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
