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
    // The only join row that is counted. The rest -- egg groups, variant types, stat changes -- are
    // read back with the entity they hang off, so a dropped one shows up as a hole in that entity.
    // Nothing else counts the learnset, and half of it going missing would look like a quiet dataset.
    val learnerCount: Int,
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
 * Both halves of `effect_entries` ship, and neither is `flavor_text_entries`: the effect entries are
 * PokeAPI's own prose under BSD, while flavour text is verbatim copyrighted game text that #10
 * forbids shipping. See #11, which settled this for every entity type. [shortEffect] is the line a
 * card shows; [effect] is the paragraph the detail screen shows under it, and the two are written
 * for those two jobs rather than one being a truncation of the other.
 *
 * **There is no category, tag or trigger field here**, and #27 asks for one. Three attempts at it are
 * recorded in `DECISIONS.md § Rejected for now: a classification for abilities`; the short version is
 * that upstream has no such field, and the taxonomies invented to replace it were not good enough to
 * commit to a dataset this hard to change. See #65.
 */
@Serializable
data class AbilityJson(
    val slug: String,
    val name: String,
    val generation: Int,
    val shortEffect: String,
    val effect: String,
)

/**
 * One move.
 *
 * [power] and [accuracy] are nullable because they are genuinely absent, not because a default was
 * unavailable: 331 moves inflict no damage and 285 cannot miss. [pp] is nullable for the same kind
 * of reason, and [shortEffect] because upstream has written none for 93 of the Generation VIII and
 * IX moves -- a hole the screen shows as absent rather than filling with prose it invented.
 *
 * [statChanges] sits beside [meta] rather than inside it, which is upstream's own shape and matters
 * here for a concrete reason: fifteen moves have stat changes and no meta row at all. Nesting them
 * would drop the fact that Trailblaze raises Speed.
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
    val effect: String?,
    val meta: MoveMetaJson?,
    // Stat stages the move moves, keyed by stat slug and signed: {"attack": -1}. Empty for the 745
    // moves that move none, so a screen can ask the map rather than ask whether there is one.
    val statChanges: Map<String, Int>,
)

/**
 * The mechanical detail behind a move's prose -- how many times it hits, what it inflicts, how much
 * it drains. Upstream's `move_meta`, and null for the 92 Generation VIII and IX moves it has not
 * filled in yet.
 *
 * **A number here is null rather than 0 when there is nothing to say.** Upstream writes 0 for "no
 * drain", "no flinch chance" and "normal crit rate" alike; carrying that through would make every
 * reader re-derive which zeroes are facts.
 *
 * [ailmentChance] is the trap that rule exists for. Thirty-six moves name an ailment and store a
 * chance of 0, which means *always* -- Thunder Wave does not paralyse 0% of the time. It is null
 * here, so null beside a non-null [ailment] reads as certainty. [statChance] says the same about
 * [MoveJson.statChanges]: Growl always lowers Attack.
 *
 * [drain] and [healing] are signed and the sign is the meaning. Drain is a share of the damage
 * dealt, negative for recoil; healing is a share of the user's own maximum HP, negative for the two
 * moves that cost HP to use.
 */
@Serializable
data class MoveMetaJson(
    // Upstream's own fourteen-way split -- "damage", "ailment", "net-good-stats", "damage-lower",
    // "ohko", "unique" and eight more. The one classification axis here that was not invented; see
    // DECISIONS.md on the ability categories that were.
    val category: String,
    // "burn", "paralysis", "confusion" and eighteen more. "unknown" for the four whose ailment
    // genuinely varies: Tri Attack picks one of three.
    val ailment: String?,
    val ailmentChance: Int?,
    val minHits: Int?,
    val maxHits: Int?,
    val minTurns: Int?,
    val maxTurns: Int?,
    val drain: Int?,
    val healing: Int?,
    // Stages above the normal critical-hit rate. 6 is the four moves that always crit.
    val critRate: Int?,
    val flinchChance: Int?,
    val statChance: Int?,
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

/**
 * Which Pokemon learn one move.
 *
 * Grouped by move because that is the only direction anything reads it: a move's detail screen asks
 * "who learns this", and nothing yet asks a Pokemon what it knows. The reverse join is the same rows
 * and arrives with the Pokemon detail's Moves tab.
 *
 * 166 of the 919 moves have an empty [learnedBy] and that is correct rather than missing -- they are
 * the Z-moves, the Max moves and the other battle-only entries no Pokemon is ever taught.
 */
@Serializable
data class LearnsetJson(
    val slug: String,
    val learnedBy: List<LearnerJson>,
)

/**
 * One Pokemon that learns a move, and how.
 *
 * [level] is set only for `level-up`, and not for all of those: it is null for the other three methods,
 * where the question does not arise, and for the 160 level-up moves a Pokemon knows without being
 * taught. Upstream writes 0 in every one of those cases and 0 is not a level.
 */
@Serializable
data class LearnerJson(
    val variant: String,
    val method: String,
    val level: Int?,
)
