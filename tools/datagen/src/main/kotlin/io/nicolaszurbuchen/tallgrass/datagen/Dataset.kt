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
    val name: String,
    val formLabel: String?,
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
    val name: String,
    val isHidden: Boolean,
    val slot: Int,
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
