package io.nicolaszurbuchen.tallgrass.datagen

import kotlinx.serialization.Serializable

/**
 * The shape of the committed JSON.
 *
 * These are the reviewable artefact. Every field here shows up in a pull-request diff when the
 * pinned SHA moves, which is the whole reason the pipeline has a JSON stage at all instead of going
 * straight from CSV to SQLite.
 */
@Serializable
data class Manifest(
    val schemaVersion: Int,
    val sourceSha: String,
    val speciesCount: Int,
    val variantCount: Int,
    val listedVariantCount: Int,
    val typeCount: Int,
    val abilityCount: Int,
    val moveCount: Int,
)

@Serializable
data class SpeciesJson(
    val dexNumber: Int,
    val slug: String,
    val name: String,
    val genus: String,
    val generation: Int,
    val genderRate: Int,
    val captureRate: Int,
    val hatchCounter: Int,
    val growthRate: String,
    val isBaby: Boolean,
    val isLegendary: Boolean,
    val isMythical: Boolean,
    val eggGroups: List<String>,
)

@Serializable
data class VariantJson(
    val slug: String,
    val speciesDexNumber: Int,
    val speciesSlug: String,
    val name: String,
    val formLabel: String?,
    val form: String?,
    val formKind: FormKind,
    val isMega: Boolean,
    val isBattleOnly: Boolean,
    val isDefault: Boolean,
    val listedInDex: Boolean,
    val height: Int,
    val weight: Int,
    val artworkUrl: String,
    val sortOrder: Int,
    val types: List<String>,
    val stats: Map<String, Int>,
    val abilities: List<AbilityRefJson>,
)

@Serializable
data class AbilityRefJson(
    val slug: String,
    val isHidden: Boolean,
    val slot: Int,
)

/**
 * One ability.
 *
 * [shortEffect] is `effect_entries.short_effect` and never `flavor_text_entries`: the short effect is
 * PokeAPI's own structured summary under BSD, while flavour text is verbatim copyrighted game text
 * that #10 forbids shipping. See #11, which settled this for every entity type.
 *
 * [category] is the classifier's answer unless `ability-categories.json` overrides it.
 */
@Serializable
data class AbilityJson(
    val slug: String,
    val name: String,
    val generation: Int,
    val category: AbilityCategory,
    val shortEffect: String,
)

/**
 * One move.
 *
 * [power] and [accuracy] are nullable because they are genuinely absent, not because a default was
 * unavailable: 331 moves inflict no damage and 285 cannot miss. [pp] is nullable for the same kind
 * of reason, and [shortEffect] because upstream has written none for 93 of the Generation VIII and
 * IX moves -- a hole the screen shows as absent rather than filling with prose it invented.
 */
@Serializable
data class MoveJson(
    val slug: String,
    val name: String,
    val generation: Int,
    val type: String,
    // "physical", "special" or "status", which is the axis the icon on a move card draws.
    val damageClass: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val priority: Int,
    val target: String,
    // The percentage on "has a chance to burn the target", which the prose deliberately leaves out.
    val effectChance: Int?,
    val shortEffect: String?,
)

@Serializable
data class TypeJson(
    val slug: String,
    val name: String,
    val generation: Int,
)

@Serializable
data class TypeEfficacyJson(
    val damage: String,
    val target: String,
    val factorPercent: Int,
)

@Serializable
data class TypeChartJson(
    val types: List<TypeJson>,
    val efficacies: List<TypeEfficacyJson>,
)
