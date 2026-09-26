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
    val regionCount: Int,
    val locationCount: Int,
    val versionCount: Int,
    // Counted for the same reason as the learnset: the encounters are split across eleven files and
    // nothing else would notice one of them coming back short.
    val encounterSlotCount: Int,
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

/**
 * One battle-distinct form.
 *
 * [baseExperience] and [evYield] are what defeating this form is worth, and both are **absent for
 * the forms upstream has not filled in yet** -- the 49 Legends Z-A Megas, which carry an empty
 * `base_experience` and a zero against every stat's `effort`. Written as a null and an empty map
 * rather than as a 0 and six zeroes, because a Pokemon that awards nothing is not a thing: every
 * filled-in form is worth experience and yields between one and three effort values. See
 * `DECISIONS.md § A move's absent numbers are absent, not zero`, which is the same rule.
 *
 * [evYield] holds only the stats that award something, for the same reason `move.meta` drops its
 * defaults: carried whole, 1,385 forms would each list four stats they yield nothing against and
 * every reader would have to re-derive which zeroes were facts.
 */
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
    val baseExperience: Int?,
    val artworkUrl: String,
    val sortOrder: Int,
    val types: List<String>,
    val stats: Map<String, Int>,
    val evYield: Map<String, Int>,
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

/**
 * One playable version, which is one cell of the availability grid.
 *
 * Every version is its own cell and no two share one: FireRed and LeafGreen do not share an encounter
 * table, so they cannot share a cell, and nor can Red and Blue or Sun and Moon. See #9.
 *
 * [code] is unique only within [console], which is the grouping's real job rather than decoration --
 * `Y` is Yellow on the GB row and Pokemon Y on the 3DS row.
 *
 * [regions] is usually one and occasionally two: Gold, Silver and Crystal are set in Johto and reach
 * Kanto as a second region, and their cells appear on both regions' grids.
 */
@Serializable
data class VersionJson(
    val slug: String,
    val name: String,
    val code: String,
    val console: Console,
    val generation: Int,
    val versionGroup: String,
    val regions: List<String>,
)

/**
 * One region, and the three facts about it upstream does not carry.
 *
 * [blurb] and [boxArt] are curated; see `CURATED_REGIONS`, which explains why. Everything else here is
 * derived, including [nativeName], which is free in upstream's names table.
 *
 * [nativeName] is null for exactly one region. Orre is the only one upstream has no Japanese name for,
 * which is of a piece with the rest of it -- no regional dex either, and two games nothing else in the
 * dataset references.
 *
 * [pokedex] is empty for Orre and holds more than one entry only for Kalos, whose three dexes are each
 * a third of the region. See `CURATED_REGIONS`.
 */
@Serializable
data class RegionJson(
    val slug: String,
    val name: String,
    val nativeName: String?,
    val generation: Int,
    val blurb: String,
    val boxArt: List<String>,
    val pokedex: List<RegionDexEntryJson>,
    // Every version set in this region, in the order the grid draws them: newest console first.
    val versions: List<String>,
    val locationCount: Int,
)

/**
 * One entry in a regional Pokedex.
 *
 * [number] is the regional entry number and has nothing to do with the national Dex number -- Chikorita
 * is #001 in Johto and #152 nationally.
 *
 * [variant] is the **region-native** form, which is the whole point of a regional dex and the
 * correction #5 records: Kanto's #037 is `vulpix`, Alola's is `vulpix-alola`. The two must never be
 * conflated with `listedInDex`, which is a National-dex question and true for both.
 */
@Serializable
data class RegionDexEntryJson(
    val number: Int,
    val variant: String,
)

/**
 * One place, which is what the location detail screen is about.
 *
 * The screen is per **location** and not per area: Canalave City's several fishing spots read as one
 * place with several rods, which is #24's call.
 *
 * [areas] survives that folding anyway, and has to. An area is the unit a rate is a percentage *of*,
 * and 14.5% of this dataset's (version, location, method) groups draw from more than one of them --
 * up to 22. Fold them together in the data and a rate bar sums to 2,200%. So the areas are carried
 * here, the denominator stays correct, and the folding is a thing the screen does rather than a thing
 * the dataset did to it.
 *
 * [category] is read out of the slug and is decoration; see [LocationCategory].
 */
@Serializable
data class LocationJson(
    val slug: String,
    val name: String,
    val region: String,
    val category: LocationCategory,
    val areas: List<LocationAreaJson>,
    // Versions with at least one encounter here, which is not the same as the versions the place
    // exists in: a location with none is listed and says so. See #24 on Berry Forest.
    val versions: List<String>,
)

/**
 * One sub-area of a place.
 *
 * [name] is null when the area *is* the place -- upstream leaves the identifier empty for a location's
 * main area, and names it after the location itself in the prose table. A screen showing that name
 * beside the location's own heading would be repeating itself, so null means "do not label this".
 */
@Serializable
data class LocationAreaJson(
    val slug: String,
    val name: String?,
)

/**
 * Every encounter in one region, nested location -> version -> table.
 *
 * **One file per region**, because flat and pretty-printed this is a single 28 MB file that no diff
 * viewer will open, and #10 wants the committed JSON reviewable above all else. Nesting also pays for
 * itself: the location and version slugs are written once each instead of 100,439 times.
 */
@Serializable
data class RegionEncountersJson(
    val region: String,
    val locations: List<LocationEncountersJson>,
)

@Serializable
data class LocationEncountersJson(
    val location: String,
    val versions: List<VersionEncountersJson>,
)

@Serializable
data class VersionEncountersJson(
    val version: String,
    val tables: List<EncounterTableJson>,
)

/**
 * One encounter table: an area, a method, and everything that can turn up in it.
 *
 * **This is the unit that sums to 100%**, and it is (area, method) rather than method alone for the
 * reason [LocationJson.areas] exists. Merging Old Rod into Good Rod, or two areas' walking tables into
 * one, produces bars summing past 200% -- see #24, which is why the method tabs are the actual method
 * and never a group like "Fishing".
 *
 * The slots inside still do not all apply at once: each carries its own conditions, and a complete
 * condition state picks the subset that sums to 100. See [EncounterSlotJson].
 */
@Serializable
data class EncounterTableJson(
    val area: String,
    val method: String,
    val slots: List<EncounterSlotJson>,
)

/**
 * One thing that can appear in one table, and how often.
 *
 * [chance] is a share of the table, already summed across the slots a variant occupies more than once
 * -- upstream stores twelve slots and one Pokemon may hold four of them. Summing stops at the table,
 * never across tables, and never across condition states.
 *
 * [conditions] is the trap. The tags are **AND-filters on this slot**, not alternative tables: a slot
 * counts only when every tag it carries is satisfied. Kanto's Route 1 in HeartGold holds 42 slots that
 * look like 360% of a walking table until you see that they are fragments of one twelve-slot table,
 * each tagged with the state that switches it on. Every complete state resolves to exactly 100%. See
 * the correction on #8, which is the reason these are preserved per slot rather than aggregated away
 * at generation time -- doing that is unrecoverable.
 *
 * Empty for the majority of slots, which apply always.
 */
@Serializable
data class EncounterSlotJson(
    val variant: String,
    val minLevel: Int,
    val maxLevel: Int,
    val chance: Int,
    val conditions: List<String>,
)

/**
 * One axis a table can be conditioned on, and the states it can be in.
 *
 * Upstream's thirty conditions and 361 values, of which this dataset uses the ones its encounters
 * actually reference. [isDefault] marks the state the games treat as ordinary -- `swarm-no`,
 * `time-day` -- which is not the same as the app's "Any", and is not what the screen defaults to:
 * "Any" means *unpinned*, showing the best case across every state, and is a fourth option rather
 * than one of these.
 */
@Serializable
data class EncounterConditionJson(
    val slug: String,
    val axis: String,
    val isDefault: Boolean,
)
