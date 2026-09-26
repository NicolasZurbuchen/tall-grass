package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * What is true of a Pokemon regardless of which form is on screen.
 *
 * Everything here is species-level on purpose. Breeding does not change with the form — Alolan
 * Vulpix keeps Vulpix's egg groups — and keeping the two apart is what the Species/Variant split
 * exists for. See #5.
 */
data class PokemonSpecies(
    val dexNumber: Int,
    val name: String,
    val genus: String,
    /** Eighths of a chance of being female, or -1 for a genderless species. Upstream's encoding. */
    val genderRate: Int,
    /**
     * How likely a ball is to hold, 0-255, where higher is easier. Upstream's scale.
     *
     * Read by the detail's Location tab, which is where #43 put it: catch rate is a fact about
     * *meeting* a Pokemon rather than about raising one, and under the About tab's Training heading
     * it read as a stat you improve.
     */
    val captureRate: Int,
    val hatchCounter: Int,
    val growthRate: GrowthRate,
    val eggGroups: List<EggGroup>,
)
