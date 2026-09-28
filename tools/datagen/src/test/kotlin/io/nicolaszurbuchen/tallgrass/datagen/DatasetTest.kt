package io.nicolaszurbuchen.tallgrass.datagen

import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the committed dataset rather than the code that wrote it.
 *
 * The failure this exists for is a generation that half-worked: a truncated read, a filter that
 * matched nothing, a regional rule that quietly stopped firing. All of those produce a dataset that
 * parses, builds a database and ships a dex with holes in it.
 *
 * The counts are deliberately exact. A range would pass through exactly the drift worth catching,
 * and when the pinned SHA moves these are *meant* to need updating -- that edit is the moment
 * somebody looks at what changed.
 */
class DatasetTest {
    private val json = Json { ignoreUnknownKeys = false }
    private val dataDir = File("../../data")

    private val manifest by lazy { json.decodeFromString<Manifest>(dataDir.resolve("manifest.json").readText()) }
    private val species by lazy { json.decodeFromString<List<SpeciesJson>>(dataDir.resolve("species.json").readText()) }
    private val variants by lazy { json.decodeFromString<List<VariantJson>>(dataDir.resolve("variants.json").readText()) }
    private val types by lazy { json.decodeFromString<TypeChartJson>(dataDir.resolve("types.json").readText()) }
    private val abilities by lazy { json.decodeFromString<List<AbilityJson>>(dataDir.resolve("abilities.json").readText()) }
    private val moves by lazy { json.decodeFromString<List<MoveJson>>(dataDir.resolve("moves.json").readText()) }
    private val learnset by lazy { json.decodeFromString<List<LearnsetJson>>(dataDir.resolve("learnset.json").readText()) }
    private val versions by lazy { json.decodeFromString<List<VersionJson>>(dataDir.resolve("versions.json").readText()) }
    private val regions by lazy { json.decodeFromString<List<RegionJson>>(dataDir.resolve("regions.json").readText()) }
    private val locations by lazy { json.decodeFromString<List<LocationJson>>(dataDir.resolve("locations.json").readText()) }
    private val conditions by lazy {
        json.decodeFromString<List<EncounterConditionJson>>(dataDir.resolve("encounter-conditions.json").readText())
    }

    // Read in the regions' own order, which is the order the ids were assigned in when the database
    // was built. Reading the directory instead would make that a function of the filesystem.
    private val encounters by lazy {
        regions.mapNotNull { region ->
            dataDir.resolve("encounters/${region.slug}.json")
                .takeIf { it.exists() }
                ?.let { json.decodeFromString<RegionEncountersJson>(it.readText()) }
        }
    }

    @Test
    fun manifest_matchesTheFilesItDescribes() {
        assertEquals(manifest.speciesCount, species.size)
        assertEquals(manifest.variantCount, variants.size)
        assertEquals(manifest.listedVariantCount, variants.count { it.listedInDex })
        assertEquals(manifest.typeCount, types.types.size)
        assertEquals(manifest.abilityCount, abilities.size)
        assertEquals(manifest.moveCount, moves.size)
        assertEquals(manifest.learnerCount, learnset.sumOf { it.learnedBy.size })
        assertEquals(manifest.regionCount, regions.size)
        assertEquals(manifest.locationCount, locations.size)
        assertEquals(manifest.versionCount, versions.size)
        assertEquals(
            manifest.encounterSlotCount,
            encounters.sumOf { region ->
                region.locations.sumOf { location -> location.versions.sumOf { it.tables.sumOf { table -> table.slots.size } } }
            },
        )
    }

    @Test
    fun dataset_holdsEveryNationalDexNumberExactlyOnce() {
        assertEquals(1025, species.size)
        assertEquals((1..1025).toList(), species.map { it.dexNumber })
    }

    @Test
    fun types_areTheEighteenRealOnes() {
        assertEquals(18, types.types.size)
        assertTrue(types.types.none { it.slug == "stellar" || it.slug == "unknown" || it.slug == "shadow" })
    }

    @Test
    fun everyVariant_belongsToAKnownSpecies() {
        val dexNumbers = species.map { it.dexNumber }.toSet()
        val orphans = variants.filterNot { it.speciesDexNumber in dexNumbers }
        assertTrue(orphans.isEmpty(), "Variants with no species: ${orphans.map { it.slug }}")
    }

    @Test
    fun everyVariant_hasArtworkAndAtLeastOneType() {
        val untyped = variants.filter { it.types.isEmpty() }
        assertTrue(untyped.isEmpty(), "Variants with no type: ${untyped.map { it.slug }}")
        assertTrue(variants.all { it.artworkUrl.endsWith(".png") })
    }

    /**
     * **The check that catches an id from the wrong table.**
     *
     * Upstream numbers `pokemon` and `pokemon_forms` separately and both run into the ten-thousands,
     * so a form id pasted into an artwork URL resolves — to somebody else. Every Arceus Plate and
     * Silvally Memory shipped with another Pokemon's picture that way: Dragon Arceus was Mega Mewtwo
     * X, because 10043 is a real `pokemon` id belonging to it.
     *
     * Nothing else notices. The URL is well-formed, the image loads, and the only way to see it is
     * to open the form switcher and recognise the Pokemon looking back.
     */
    @Test
    fun noTwoVariants_shareOnePicture() {
        val shared =
            variants.groupBy { it.artworkUrl }
                .filterValues { it.size > 1 }
                .mapValues { (_, group) -> group.map { it.slug } }

        assertTrue(shared.isEmpty(), "One picture used by several variants: $shared")
    }

    @Test
    fun everySpecies_isListedInTheDex() {
        // A species with no listed variant would be a gap in the grid at that Dex number.
        val listedNumbers = variants.filter { it.listedInDex }.map { it.speciesDexNumber }.toSet()
        val missing = species.map { it.dexNumber }.filterNot { it in listedNumbers }
        assertTrue(missing.isEmpty(), "Dex numbers with no card: $missing")
    }

    @Test
    fun onlyTheDefaultForm_isListedInTheDex() {
        fun listed(slug: String) = variants.single { it.slug == slug }.listedInDex

        assertTrue(listed("vulpix"), "The species' own form is the card")

        assertTrue(!listed("vulpix-alola"), "A regional form is reached through the form switcher")
        assertTrue(!listed("wooper-paldea"))
        assertTrue(!listed("tauros-paldea-aqua-breed"))
        assertTrue(!listed("darmanitan-galar-standard"))
        assertTrue(!listed("charizard-mega-x"), "A Mega is a battle state, not a browse entry")
        assertTrue(!listed("darmanitan-galar-zen"))
        assertTrue(!listed("meowstic-female"), "Gender differences stay behind the form switcher")
        assertTrue(!listed("zygarde-complete"))
    }

    @Test
    fun theDex_holdsExactlyOneCardPerSpecies() {
        // The invariant the browse list is built on: scrolling it is walking the National Dex, and
        // every form of every kind is reached from the card of the species it belongs to.
        assertEquals(species.size, variants.count { it.listedInDex })
    }

    @Test
    fun pikachusCostumes_doNotEachGetACard() {
        // `alola-cap` is a hat from Alola, not an Alolan Pikachu: prefix-matching the region name
        // would put a second Pikachu in the grid differing only in headwear.
        val pikachus = variants.filter { it.slug.startsWith("pikachu") }
        assertTrue(pikachus.size > 1, "Sanity: the costumes should be in the dataset")
        assertEquals(1, pikachus.count { it.listedInDex })
    }

    @Test
    fun cosmeticForms_neverBecameVariants() {
        // Alcremie has 63 cosmetic combinations upstream and exactly one battle-distinct form.
        assertTrue(variants.count { it.slug.startsWith("alcremie") } <= 2)
    }

    @Test
    fun variants_carryTheTrainingFiguresTheAboutTabNeeds() {
        val butterfree = variants.single { it.slug == "butterfree" }

        assertEquals(178, butterfree.baseExperience)
        assertEquals(mapOf("special-attack" to 2, "special-defense" to 1), butterfree.evYield)
    }

    @Test
    fun trainingFigures_moveWithTheFormRatherThanTheSpecies() {
        // The reason they sit on the variant rather than the species. 95 forms are worth a different
        // amount of experience than their default form and 42 award a different stat, so reading
        // either off the species would report the switcher's other pages wrong.
        assertEquals(240, variants.single { it.slug == "charizard" }.baseExperience)
        assertEquals(285, variants.single { it.slug == "charizard-mega-x" }.baseExperience)

        // A regional form is the sharper case: Dugtrio trains Speed and the Alolan one trains
        // Attack, which is a different answer rather than a larger one.
        assertEquals(mapOf("speed" to 2), variants.single { it.slug == "dugtrio" }.evYield)
        assertEquals(mapOf("attack" to 2), variants.single { it.slug == "dugtrio-alola" }.evYield)
    }

    @Test
    fun aFormIsEitherCostedOrNotCostedAtAll() {
        // 49 forms carry no base experience, all of them Legends Z-A Megas, and they are exactly the
        // 49 that award no effort against any stat. That the two sets coincide is what says these
        // are figures upstream has not written rather than a column that failed to read -- and it is
        // what lets the About tab drop both rows on one test.
        val noExperience = variants.filter { it.baseExperience == null }.map { it.slug }.toSet()
        val noYield = variants.filter { it.evYield.isEmpty() }.map { it.slug }.toSet()

        assertEquals(49, noExperience.size)
        assertEquals(noExperience, noYield)

        val notMegas = noExperience.filterNot { it.contains("-mega") }
        assertTrue(notMegas.isEmpty(), "Uncosted forms that are not Megas: $notMegas")
    }

    @Test
    fun aCostedForm_awardsAtLeastOneEffortValue() {
        // The rule that makes "awards nothing" readable as "upstream has not said" rather than as a
        // Pokemon worth no effort. A real zero would break it and nothing else would notice, so the
        // whole distribution is pinned: three is the cap the games use, and `terapagos-terastal` is
        // upstream's one row that exceeds it -- 2 Defense and 2 Special Defense, on a battle-only
        // form. Left as upstream wrote it, because capping it would be inventing a figure.
        val totals = variants.groupingBy { it.evYield.values.sum() }.eachCount()

        assertEquals(mapOf(0 to 49, 1 to 388, 2 to 564, 3 to 383, 4 to 1), totals)
        assertEquals(4, variants.single { it.slug == "terapagos-terastal" }.evYield.values.sum())
    }

    @Test
    fun evYield_namesOnlyTheStatsTheStatsBlockDoes() {
        // A yield against a stat the variant has no base figure for would draw a row the Stats tab
        // cannot: upstream's `special` (id 9) is the one that could appear that way.
        val stray = variants.filter { variant -> variant.evYield.keys.any { it !in variant.stats.keys } }

        assertTrue(stray.isEmpty(), "Variants yielding against a stat they do not have: ${stray.map { it.slug }}")
    }

    @Test
    fun species_carryTheBreedingDataTheAboutTabNeeds() {
        val bulbasaur = species.single { it.dexNumber == 1 }
        assertEquals("bulbasaur", bulbasaur.slug)
        assertEquals("Bulbasaur", bulbasaur.name)
        assertTrue(bulbasaur.genus.isNotEmpty())
        assertTrue(bulbasaur.eggGroups.isNotEmpty())
        assertEquals("medium-slow", bulbasaur.growthRate)
    }

    @Test
    fun genderlessSpecies_keepUpstreamsMinusOne() {
        // -1 is a third case rather than a missing percentage, and flattening it to 0 would render
        // Magnemite as female-only.
        assertEquals(-1, species.single { it.slug == "magnemite" }.genderRate)
    }

    @Test
    fun typeChart_storesOnlyTheNonNeutralPairs() {
        assertTrue(types.efficacies.none { it.factorPercent == 100 })
        assertEquals(
            200,
            types.efficacies.single { it.damage == "water" && it.target == "fire" }.factorPercent,
        )
        assertEquals(
            0,
            types.efficacies.single { it.damage == "normal" && it.target == "ghost" }.factorPercent,
        )
    }

    @Test
    fun json_isSortedSoTheDiffIsReadable() {
        assertEquals(species.map { it.dexNumber }.sorted(), species.map { it.dexNumber })
        assertEquals(types.types.map { it.slug }.sorted(), types.types.map { it.slug })
        assertEquals(abilities.map { it.slug }.sorted(), abilities.map { it.slug })
        assertEquals(moves.map { it.slug }.sorted(), moves.map { it.slug })
    }

    @Test
    fun everyVariant_isClassified() {
        // The partition must be total. A variant with no kind is one the classifier fell through,
        // which is the failure the ordering in classifyForm exists to prevent.
        assertEquals(
            mapOf(
                FormKind.NONE to 1025,
                FormKind.MEGA to 97,
                FormKind.ALTERNATE to 97,
                FormKind.REGIONAL to 57,
                FormKind.COSMETIC to 36,
                FormKind.GIGANTAMAX to 34,
                FormKind.BATTLE_ONLY to 23,
                FormKind.TOTEM to 12,
                FormKind.GENDER to 4,
            ),
            variants.groupingBy { it.formKind }.eachCount().toList().sortedByDescending { it.second }.toMap(),
        )
    }

    @Test
    fun onlyDefaultVariants_areClassifiedNone() {
        assertEquals(
            variants.count { it.isDefault },
            variants.count { it.formKind == FormKind.NONE },
        )
    }

    @Test
    fun formsDifferingOnlyByAbility_areNotFiledAsCostumes() {
        // The regression this pins: a COSMETIC test comparing stats and types alone puts all eight
        // of these in COSMETIC, because an ability is the only thing that separates them from their
        // base form.
        listOf(
            "greninja-battle-bond",
            "rockruff-own-tempo",
            "toxtricity-low-key",
            "zygarde-50-power-construct",
            "basculin-blue-striped",
            "squawkabilly-yellow-plumage",
            "squawkabilly-white-plumage",
        ).forEach { slug ->
            assertEquals(FormKind.ALTERNATE, variants.single { it.slug == slug }.formKind, slug)
        }
    }

    @Test
    fun costumesAndRideForms_areCosmetic() {
        listOf("pikachu-rock-star", "pikachu-cosplay", "koraidon-sprinting-build", "miraidon-glide-mode")
            .forEach { slug ->
                assertEquals(FormKind.COSMETIC, variants.single { it.slug == slug }.formKind, slug)
            }
    }

    @Test
    fun everyVariant_namesItsSpeciesBySlug() {
        val slugs = species.map { it.slug }.toSet()
        val orphans = variants.filterNot { it.speciesSlug in slugs }
        assertTrue(orphans.isEmpty(), "Variants naming an unknown species: ${orphans.map { it.slug }}")
    }

    @Test
    fun abilities_carryNoName() {
        // The name belongs to the ability table that arrives with #27. Storing it here repeated it
        // across 2,943 rows holding 313 distinct abilities.
        assertTrue(variants.all { variant -> variant.abilities.all { it.slug.isNotEmpty() } })
    }

    @Test
    fun arceusAndSilvally_keepTheirTypeChangingForms() {
        // Upstream files these as forms rather than Pokemon because only the type moves. Left out,
        // the dataset says arceus [normal] and the Stats tab reports Fire Arceus as weak to
        // Fighting.
        assertEquals(18, variants.count { it.slug.startsWith("arceus") })
        assertEquals(18, variants.count { it.slug.startsWith("silvally") })

        val fire = variants.single { it.slug == "arceus-fire" }
        assertEquals(listOf("fire"), fire.types)
        assertEquals(FormKind.ALTERNATE, fire.formKind)
        assertTrue(!fire.listedInDex, "Eighteen Arceus would bury the rest of the grid")

        // Artwork comes from the form id, not the pokemon id, or all eighteen share one picture.
        assertTrue(fire.artworkUrl != variants.single { it.slug == "arceus" }.artworkUrl)
    }

    @Test
    fun promotedForms_inheritWhatUpstreamCannotVary() {
        // There is no per-form stat or ability table, so these carry the base form's. The type is
        // the only thing that genuinely differs.
        val base = variants.single { it.slug == "arceus" }
        val fire = variants.single { it.slug == "arceus-fire" }

        assertEquals(base.stats, fire.stats)
        assertEquals(base.abilities.map { it.slug }, fire.abilities.map { it.slug })
        assertEquals(base.speciesDexNumber, fire.speciesDexNumber)
    }

    // region abilities

    @Test
    fun abilities_areTheMainSeriesOnesOnly() {
        // Upstream's other sixty are Pokemon Conquest's, numbered from 10000 and flagged
        // is_main_series = 0. None has effect text in any language and none is on any Pokemon, so a
        // card for Mountaineer would be a name over an empty space.
        assertEquals(314, abilities.size)

        val conquest = listOf("mountaineer", "wave-rider", "skater", "herbivore", "conqueror")
        val leaked = abilities.filter { it.slug in conquest }
        assertTrue(leaked.isEmpty(), "Spin-off abilities in the dataset: ${leaked.map { it.slug }}")
    }

    @Test
    fun everyAbility_carriesTheTextTheScreenDraws() {
        val silent = abilities.filter { it.shortEffect.isBlank() }
        assertTrue(silent.isEmpty(), "Abilities with no effect text: ${silent.map { it.slug }}")
        assertTrue(abilities.all { it.effect.isNotBlank() })
        assertTrue(abilities.all { it.name.isNotBlank() })
    }

    @Test
    fun everyAbilityAVariantHas_existsInTheAbilityTable() {
        // The join the ability detail's "known by" section reads, in the direction that can break:
        // variantAbility holds a slug and nothing enforces that the ability exists.
        val known = abilities.map { it.slug }.toSet()
        val dangling = variants.flatMap { it.abilities }.map { it.slug }.filterNot { it in known }.distinct()

        assertTrue(dangling.isEmpty(), "Variants naming an ability that is not in the dataset: $dangling")
    }

    @Test
    fun oneAbility_isOnNoPokemonAndThatIsCorrect() {
        // Ogerpon's, which upstream gates behind a form in a way its CSVs do not join. Pinned so
        // that an empty "known by" reads as a known fact rather than as a join that broke.
        val used = variants.flatMap { it.abilities }.map { it.slug }.toSet()
        assertEquals(listOf("embody-aspect"), abilities.map { it.slug }.filterNot { it in used })
    }

    // endregion

    // region moves

    @Test
    fun moves_areTheMainSeriesOnesOnly() {
        // Upstream's other eighteen are Pokemon XD's Shadow moves, on the same threshold as the
        // Conquest abilities.
        assertEquals(919, moves.size)
        assertTrue(moves.none { it.slug.startsWith("shadow-") && it.type == "shadow" })
        assertTrue(moves.all { it.type in types.types.map { type -> type.slug } })
        assertEquals(setOf("physical", "special", "status"), moves.map { it.damageClass }.toSet())
    }

    @Test
    fun moves_leaveGenuinelyAbsentFieldsNull() {
        // None of these is a missing value standing in for a default. A status move has no power,
        // a never-miss move has no accuracy, and 93 Generation VIII and IX moves have no effect
        // text because upstream has written none -- they carry no effect id at all.
        assertEquals(331, moves.count { it.power == null })
        assertEquals(285, moves.count { it.accuracy == null })
        assertEquals(93, moves.count { it.shortEffect == null })
        assertEquals(93, moves.count { it.effect == null })

        // PP is the counter-example, and is why the three above are worth pinning: every move has
        // one, so a null there would be a read that went wrong rather than a fact.
        assertEquals(0, moves.count { it.pp == null })
    }

    @Test
    fun everyMoveWithoutEffectText_isRecent() {
        // If this ever fails it is not a data gap, it is a parse that dropped a column: upstream has
        // written prose for everything up to Generation VII.
        val old = moves.filter { it.shortEffect == null && it.generation < 8 }
        assertTrue(old.isEmpty(), "Moves older than Gen VIII with no effect text: ${old.map { it.slug }}")
    }

    @Test
    fun moves_carryTheNumbersACardDraws() {
        val tackle = moves.single { it.slug == "tackle" }
        assertEquals("Tackle", tackle.name)
        assertEquals("normal", tackle.type)
        assertEquals("physical", tackle.damageClass)
        assertEquals(40, tackle.power)
        assertEquals(100, tackle.accuracy)
        assertEquals(35, tackle.pp)
        assertEquals(0, tackle.priority)

        // Priority is signed, and a move that always goes last is the case a non-negative read would
        // silently flatten.
        assertTrue(moves.any { it.priority > 0 } && moves.any { it.priority < 0 })
    }

    @Test
    fun moveMeta_isAbsentOnlyWhereUpstreamWroteNone() {
        // 827 of the 919 have a meta row, and the 92 that do not are the same recent moves that have
        // no effect text -- minus Syrup Bomb, which has meta and no prose. A meta row appearing on
        // an older move would mean the join went wrong, not that upstream filled a gap.
        assertEquals(92, moves.count { it.meta == null })
        assertTrue(moves.none { it.meta == null && it.generation < 8 })

        val categories = moves.mapNotNull { it.meta?.category }.toSet()
        assertEquals(14, categories.size, "Upstream's fourteen: $categories")
        assertTrue("damage" in categories && "net-good-stats" in categories && "unique" in categories)
    }

    @Test
    fun moveMeta_dropsTheZeroesThatAreNotFacts() {
        // The rule the whole type is built on: a number is present only when it says something the
        // default does not. Upstream stores 0 for "no drain" and "normal crit rate" alike, so these
        // counts are what separates a fact from a filled-in column.
        val meta = moves.mapNotNull { it.meta }
        assertEquals(22, meta.count { it.drain != null })
        assertEquals(18, meta.count { it.healing != null })
        assertEquals(26, meta.count { it.critRate != null })
        assertEquals(30, meta.count { it.flinchChance != null })
        assertEquals(28, meta.count { it.minHits != null })
        assertEquals(44, meta.count { it.minTurns != null })

        // Both are signed and the sign is the meaning. Struggle heals -25% of its user's maximum HP;
        // Double-Edge drains -33% of the damage it dealt.
        assertEquals(-25, moves.single { it.slug == "struggle" }.meta?.healing)
        assertTrue(meta.any { (it.drain ?: 0) > 0 } && meta.any { (it.drain ?: 0) < 0 })
    }

    /**
     * **The 0 that means "always".**
     *
     * Upstream stores `ailment_chance = 0` on the thirty-six moves whose ailment is certain, so a
     * reader that trusts the column renders "Thunder Wave: 0% chance to paralyse" -- the most wrong
     * number this dataset could ship, and one that looks like a rendering bug rather than a data
     * one. The generator drops it, which leaves a null beside a non-null ailment meaning certainty.
     */
    @Test
    fun aGuaranteedAilment_carriesNoPercentage() {
        val thunderWave = moves.single { it.slug == "thunder-wave" }.meta
        assertEquals("paralysis", thunderWave?.ailment)
        assertEquals(null, thunderWave?.ailmentChance)

        val firePunch = moves.single { it.slug == "fire-punch" }.meta
        assertEquals("burn", firePunch?.ailment)
        assertEquals(10, firePunch?.ailmentChance)

        // The same rule against the stat changes: Growl always lowers Attack, Rock Smash sometimes
        // lowers Defense.
        val growl = moves.single { it.slug == "growl" }
        assertEquals(mapOf("attack" to -1), growl.statChanges)
        assertEquals(null, growl.meta?.statChance)
        assertEquals(50, moves.single { it.slug == "rock-smash" }.meta?.statChance)
    }

    @Test
    fun noChance_hangsOffNothing() {
        // Five moves store an ailment chance with no ailment to attach it to -- Frost Breath keeps
        // 100. A percentage that names no effect cannot be drawn, so it leaves with the ailment, and
        // this is the invariant that makes the pair safe to render: a chance implies an effect.
        val dangling = moves.filter { it.meta?.ailmentChance != null && it.meta?.ailment == null }
        assertTrue(dangling.isEmpty(), "Moves with a chance of nothing: ${dangling.map { it.slug }}")

        val statChanceWithoutStats = moves.filter { it.meta?.statChance != null && it.statChanges.isEmpty() }
        assertTrue(statChanceWithoutStats.isEmpty(), "Moves with a stat chance and no stats: $statChanceWithoutStats")
    }

    @Test
    fun aVaryingAilment_saysSoRatherThanNamingOne() {
        // Tri Attack picks one of burn, freeze and paralysis. Upstream files that as -1, and
        // flattening it to null would leave the 20% hanging off nothing -- which the test above
        // would then read as correct.
        assertEquals("unknown", moves.single { it.slug == "tri-attack" }.meta?.ailment)
        assertEquals(20, moves.single { it.slug == "tri-attack" }.meta?.ailmentChance)
        assertEquals(4, moves.count { it.meta?.ailment == "unknown" })
    }

    @Test
    fun statChanges_surviveAMissingMetaRow() {
        // Why statChanges is a sibling of meta rather than a field inside it. Fifteen moves have
        // stat changes and no meta row at all; nesting them would silently drop every one.
        val orphans = moves.filter { it.statChanges.isNotEmpty() && it.meta == null }
        assertEquals(15, orphans.size)
        assertEquals(mapOf("speed" to 1), moves.single { it.slug == "trailblaze" }.statChanges)

        assertEquals(174, moves.count { it.statChanges.isNotEmpty() })
        assertTrue(moves.all { move -> move.statChanges.values.all { it in -2..3 } })

        // Ancient Power raises all five, and the map is written in the order the stat bars are drawn
        // rather than alphabetically.
        assertEquals(
            listOf("attack", "defense", "special-attack", "special-defense", "speed"),
            moves.single { it.slug == "ancient-power" }.statChanges.keys.toList(),
        )
    }

    // endregion

    // region learnset

    @Test
    fun learnset_coversEveryMoveAndNamesOnlyKnownPokemon() {
        assertEquals(moves.size, learnset.size, "one entry per move, empty or not")
        assertEquals(moves.map { it.slug }, learnset.map { it.slug })

        val variantSlugs = variants.map { it.slug }.toSet()
        val dangling = learnset.flatMap { it.learnedBy }.map { it.variant }.filterNot { it in variantSlugs }.distinct()

        assertTrue(dangling.isEmpty(), "Learners naming a variant that is not in the dataset: $dangling")
    }

    /**
     * **The regression this exists for cost Charizard every move it has.**
     *
     * Version group 32 is Pokemon Champions and every row in it is `train`, which is move mastery
     * rather than a way of learning anything. Read as a Pokemon's newest appearance *before* the
     * methods are filtered, it leaves 319 Pokemon with an empty learnset — and an empty list looks
     * exactly like a Pokemon that genuinely learns nothing.
     */
    @Test
    fun learnset_readsTheNewestGameThatTeachesRatherThanTheNewestGame() {
        val charizard =
            learnset.single { it.slug == "flamethrower" }.learnedBy.singleOrNull { it.variant == "charizard" }

        assertEquals("level-up", charizard?.method)
        assertEquals(46, charizard?.level)
    }

    @Test
    fun learnset_holdsOneRowPerPokemonAndMove() {
        // A move that is both a level-up move and a TM is one fact on a card, and upstream files it
        // twice. Two rows would draw the Pokemon twice in the same list.
        val duplicated =
            learnset.flatMap { entry -> entry.learnedBy.map { entry.slug to it.variant } }
                .groupingBy { it }
                .eachCount()
                .filterValues { it > 1 }

        assertTrue(duplicated.isEmpty(), "A Pokemon listed twice for one move: $duplicated")
        assertEquals(62777, learnset.sumOf { it.learnedBy.size })
    }

    @Test
    fun learnset_putsALevelOnlyWhereALevelMeansSomething() {
        // Upstream writes 0 in this column for every machine, egg and tutor row, and for the 160
        // level-up moves a Pokemon knows without being taught. Carried through, a TM would read as
        // being learned at level 0.
        val levelled = learnset.flatMap { it.learnedBy }.filter { it.level != null }

        assertTrue(levelled.all { it.method == "level-up" }, "A level on something that is not level-up")
        assertTrue(levelled.all { (it.level ?: 0) > 0 })
    }

    @Test
    fun learnset_isEmptyOnlyForMovesNobodyIsTaught() {
        // Z-moves, Max moves and the handful that only exist mid-battle. An empty list here is a fact
        // rather than a join that missed.
        assertEquals(106, learnset.count { it.learnedBy.isEmpty() })
        assertTrue(learnset.single { it.slug == "assist" }.learnedBy.isEmpty())
        assertTrue(learnset.single { it.slug == "tackle" }.learnedBy.isNotEmpty())
    }

    @Test
    fun everyPokemonWithNoLearnset_isAFormThatBorrowsOne() {
        // A Mega and a Gigantamax learn what their base form learns and upstream does not repeat the
        // rows. What would be wrong is a card in the grid with nothing behind it.
        val taught = learnset.flatMap { it.learnedBy }.map { it.variant }.toSet()
        val untaught = variants.filterNot { it.slug in taught }

        assertTrue(
            untaught.none { it.listedInDex },
            "Dex cards with no moves: ${untaught.filter { it.listedInDex }.map { it.slug }}",
        )
        assertEquals(
            setOf(FormKind.MEGA, FormKind.GIGANTAMAX, FormKind.ALTERNATE),
            untaught.map { it.formKind }.toSet(),
        )
    }

    // endregion

    @Test
    fun regions_areTheElevenAndEachIsCurated() {
        assertEquals(11, regions.size)
        assertEquals(CURATED_REGIONS.map { it.slug }.toSet(), regions.map { it.slug }.toSet())
        assertTrue(regions.all { it.blurb.isNotBlank() }, "Regions with no blurb: ${regions.filter { it.blurb.isBlank() }.map { it.slug }}")
    }

    /**
     * The list runs Kanto to Paldea and then Orre, which is upstream's own id order.
     *
     * Ordering on generation would interleave Orre with Hoenn -- both Generation III -- and put the
     * spin-off region in the middle of the main sequence.
     */
    @Test
    fun regions_runInReleaseOrderWithTheSpinOffLast() {
        assertEquals(
            listOf("kanto", "johto", "hoenn", "sinnoh", "unova", "kalos", "alola", "galar", "hisui", "paldea", "orre"),
            regions.map { it.slug },
        )
        assertEquals(3, regions.single { it.slug == "orre" }.generation)
    }

    /**
     * Orre is the region every derived field has to survive the absence of.
     *
     * Upstream has no Japanese name for it, no regional Pokedex, and -- the one that actually broke
     * the generator -- no `version_group_regions` row either, which took Colosseum and XD off the
     * only region that is nothing else.
     */
    @Test
    fun orre_hasNoNativeNameAndNoPokedexButKeepsItsGames() {
        val orre = regions.single { it.slug == "orre" }
        assertEquals(null, orre.nativeName)
        assertTrue(orre.pokedex.isEmpty(), "Orre has dex entries: ${orre.pokedex.size}")
        assertEquals(listOf("colosseum", "xd"), orre.versions)
        assertTrue(regions.filter { it.slug != "orre" }.all { it.nativeName != null })
    }

    /** Every other region has a dex, and Kanto's is the 151 everyone can check. */
    @Test
    fun everyRegionButOrre_hasARegionalPokedex() {
        val empty = regions.filter { it.pokedex.isEmpty() }
        assertEquals(listOf("orre"), empty.map { it.slug })
        assertEquals(151, regions.single { it.slug == "kanto" }.pokedex.size)
        assertEquals(242, regions.single { it.slug == "hisui" }.pokedex.size)
    }

    /**
     * #5's correction, and the reason a regional dex cannot be read off the species.
     *
     * The same Dex number resolves to a different form depending on which region is asking, and
     * `listedInDex` -- a National-dex flag, true for both -- cannot express it.
     */
    @Test
    fun regionalDex_namesTheVariantNativeToThatRegion() {
        val kanto = regions.single { it.slug == "kanto" }.pokedex
        val alola = regions.single { it.slug == "alola" }.pokedex

        assertEquals("vulpix", kanto.single { it.number == 37 }.variant)
        assertTrue(alola.any { it.variant == "vulpix-alola" }, "Alola's dex has no Alolan Vulpix")
        assertTrue(alola.none { it.variant == "vulpix" }, "Alola's dex has the Kantonian Vulpix")
    }

    /**
     * Kalos ships three Pokedexes that each number from 1, so they are concatenated rather than
     * sorted together. Sorted on the number alone they would read as three interleaved runs of
     * 1..153, and the grid would show three Chespins before anything else.
     */
    @Test
    fun kalos_concatenatesItsThreeDexesRatherThanInterleavingThem() {
        val kalos = regions.single { it.slug == "kalos" }.pokedex
        assertEquals(457, kalos.size)
        assertEquals(1, kalos.first().number)
        // The seam between Central and Coastal: the number restarts rather than carrying on.
        assertEquals(listOf(152, 153, 1, 2), kalos.drop(151).take(4).map { it.number })
        assertEquals(kalos.size, kalos.map { it.variant }.distinct().size)
    }

    @Test
    fun everyRegion_hasBoxArtNamingVariantsThatExist() {
        val slugs = variants.map { it.slug }.toSet()
        val missing = regions.flatMap { region -> region.boxArt.map { region.slug to it } }.filterNot { it.second in slugs }
        assertTrue(missing.isEmpty(), "Box art naming no variant: $missing")
        // Two regions have no legendary pair on the cover and fall back to what they have.
        assertEquals(listOf("arceus"), regions.single { it.slug == "hisui" }.boxArt)
        assertTrue(regions.filterNot { it.slug == "hisui" }.all { it.boxArt.size == 2 })
    }

    @Test
    fun everyLocation_belongsToAKnownRegion() {
        val slugs = regions.map { it.slug }.toSet()
        val orphans = locations.filterNot { it.region in slugs }
        assertTrue(orphans.isEmpty(), "Locations with no region: ${orphans.take(5).map { it.slug }}")
        assertEquals(96, locations.count { it.region == "kanto" })
    }

    @Test
    fun everyEncounter_namesAKnownVariantLocationAndVersion() {
        val variantSlugs = variants.map { it.slug }.toSet()
        val locationSlugs = locations.map { it.slug }.toSet()
        val versionSlugs = versions.map { it.slug }.toSet()

        val badVariants = mutableSetOf<String>()
        val badLocations = mutableSetOf<String>()
        val badVersions = mutableSetOf<String>()

        encounters.forEach { region ->
            region.locations.forEach { location ->
                if (location.location !in locationSlugs) badLocations += location.location
                location.versions.forEach { version ->
                    if (version.version !in versionSlugs) badVersions += version.version
                    version.tables.forEach { table ->
                        table.slots.forEach { if (it.variant !in variantSlugs) badVariants += it.variant }
                    }
                }
            }
        }

        assertTrue(badVariants.isEmpty(), "Encounters naming no variant: $badVariants")
        assertTrue(badLocations.isEmpty(), "Encounters naming no location: $badLocations")
        assertTrue(badVersions.isEmpty(), "Encounters naming no version: $badVersions")
    }

    /**
     * The correction on #8, pinned against the table it was found on.
     *
     * Condition tags are **AND-filters on a slot**, not alternative tables: a slot counts when every
     * tag it carries is satisfied by the chosen state. Read as competing tables, HeartGold's Route 1
     * walking slots look like 360% of a table. Read as one twelve-slot table whose fragments switch
     * on and off, every complete state is exactly 100%.
     *
     * If this ever fails, the pipeline has started aggregating conditions away, and that is not
     * recoverable from the output -- it has to be caught here.
     */
    @Test
    fun conditionTags_filterSlotsRatherThanNamingSeparateTables() {
        val table =
            encounters.single { it.region == "kanto" }
                .locations.single { it.location == "kanto-route-1" }
                .versions.single { it.version == "heartgold" }
                .tables.single { it.method == "walk" }

        // Summed by method alone, the way the pre-correction reading would have, this is 360%.
        assertEquals(360, table.slots.sumOf { it.chance })

        val states =
            listOf(
                setOf("swarm-no", "time-day", "radio-off"),
                setOf("swarm-no", "time-night", "radio-off"),
                setOf("swarm-no", "time-morning", "radio-off"),
                setOf("swarm-no", "time-night", "radio-sinnoh"),
            )
        states.forEach { state ->
            val sum = table.slots.filter { state.containsAll(it.conditions) }.sumOf { it.chance }
            assertEquals(100, sum, "State ${state.sorted()} sums to $sum, not 100")
        }
    }

    /**
     * Each rod is its own hundred per cent, which is why the method tabs are the actual method and
     * never a "Fishing" group. Merged, Magikarp appears at 100% under Old Rod and 55% under Good Rod
     * and the tab claims 255%.
     */
    @Test
    fun eachMethod_isItsOwnDenominator() {
        val rods =
            encounters.flatMap { it.locations }
                .flatMap { it.versions }
                .flatMap { it.tables }
                .filter { it.method in setOf("old-rod", "good-rod", "super-rod") }

        assertTrue(rods.size > 100, "Only ${rods.size} rod tables, which is too few to be reading them all")

        // Only the unconditioned tables, because summing a conditioned one across every state at
        // once is the arithmetic the correction on #8 exists to forbid -- it is how a twelve-slot
        // table reads as 360%.
        val plain = rods.filter { table -> table.slots.all { it.conditions.isEmpty() } }
        assertTrue(plain.size > 100, "Only ${plain.size} unconditioned rod tables")
        assertTrue(
            plain.none { it.slots.sumOf { slot -> slot.chance } > 200 },
            "A rod table sums past 200%, which means two rods have been merged: " +
                plain.filter { it.slots.sumOf { slot -> slot.chance } > 200 }.take(3).map { it.area to it.method },
        )
    }

    /**
     * A rarity belongs to a **slot**, so a variant holding four of a table's twelve slots is four
     * rarities to add up -- but the same slot id arriving twice is one slot written twice, and adding
     * that counts it twice.
     *
     * Upstream does exactly that for Generation II fishing. Cherrygrove's Super Rod table is stored
     * three times over, once per time of day, with the `time` condition left off all three; added up
     * it reads as 300%. Deduplicated by slot id it reads as 130 -- Krabby 60, Kingler 10, and then
     * Corsola and Staryu at 30 each, which is the day and night variance upstream did not tag.
     *
     * The residual 30 is not a bug to fix here. It is the same untagged variance Alola's walking
     * tables carry, and the reason #24 shows no running total and qualifies a rate as "up to".
     */
    @Test
    fun aRepeatedSlotIsOneSlot_notTwo() {
        val table =
            encounters.single { it.region == "johto" }
                .locations.single { it.location == "cherrygrove-city" }
                .versions.single { it.version == "silver" }
                .tables.single { it.method == "super-rod" }

        assertEquals(130, table.slots.sumOf { it.chance })
        assertEquals(60, table.slots.single { it.variant == "krabby" }.chance)
        assertEquals(
            listOf("corsola", "staryu"),
            table.slots.filter { it.chance == 30 }.map { it.variant }.sorted(),
        )
    }

    /**
     * #21 decided Generation IX ships its gap explicitly rather than hiding it. Hisui is a second
     * instance of the same gap that #21 never named -- Legends: Arceus has no encounter rows either --
     * and the screens have to say so for both.
     */
    @Test
    fun hisuiAndPaldea_haveNoEncounterDataAtAll() {
        val withData = encounters.map { it.region }.toSet()
        assertTrue("hisui" !in withData, "Hisui has encounter data now, so the empty state is wrong")
        assertTrue("paldea" !in withData, "Paldea has encounter data now, so #21's gap has closed")
        assertEquals(setOf("kanto", "johto", "hoenn", "sinnoh", "unova", "kalos", "alola", "galar", "orre"), withData)

        // Both regions still list their places. The absence is the answer, not a reason to hide them.
        assertEquals(89, locations.count { it.region == "hisui" })
        assertTrue(locations.filter { it.region == "hisui" }.all { it.versions.isEmpty() })
    }

    /**
     * The reason `LocationJson.areas` exists at all.
     *
     * #24 folds a location's areas together on screen, and folding them in the data would be wrong:
     * an area is the unit a rate is a percentage of, and one place can draw the same method from
     * several of them. Fold Sinnoh's worst case together and a walking table sums to 2,200%.
     */
    @Test
    fun oneLocation_canDrawOneMethodFromSeveralAreas() {
        val worst =
            encounters.flatMap { region -> region.locations.flatMap { location -> location.versions.map { location to it } } }
                .flatMap { (location, version) ->
                    version.tables.groupBy { it.method }.map { (method, tables) -> Triple(location.location, method, tables.size) }
                }.maxByOrNull { it.third }

        assertTrue(worst != null && worst.third > 1, "No location draws one method from several areas any more")
    }

    @Test
    fun versionCodes_areUniqueWithinTheirConsoleRow() {
        versions.groupBy { it.console }.forEach { (console, group) ->
            val codes = group.map { it.code }
            assertEquals(codes.size, codes.distinct().size, "$console has a repeated code: $codes")
        }
        // Row-local uniqueness is the whole point: these two collide and must not be made to agree.
        assertEquals("Y", versions.single { it.slug == "yellow" }.code)
        assertEquals("Y", versions.single { it.slug == "y" }.code)
    }

    /**
     * The three Japan-only Generation I releases are dropped rather than drawn: for an English
     * reader they are the games beside them, and they would put three cells on Kanto's grid that
     * cannot be told apart from three others.
     */
    @Test
    fun japanOnlyVersions_areNotShipped() {
        val slugs = versions.map { it.slug }.toSet()
        assertTrue(setOf("red-japan", "green-japan", "blue-japan").none { it in slugs })
        assertEquals(12, regions.single { it.slug == "kanto" }.versions.size)
    }

    /** Every condition an encounter references has to resolve to an axis the selector can draw. */
    @Test
    fun everyConditionTag_resolvesToAKnownAxis() {
        val known = conditions.map { it.slug }.toSet()
        val used =
            encounters.flatMap { it.locations }
                .flatMap { it.versions }
                .flatMap { it.tables }
                .flatMap { table -> table.slots.flatMap { it.conditions } }
                .toSet()

        assertTrue(used.isNotEmpty())
        assertTrue((used - known).isEmpty(), "Conditions with no axis: ${used - known}")
        assertTrue(conditions.any { it.axis == "time" } && conditions.any { it.axis == "swarm" })

        // Upstream files the Generation VIII weather values under `max-den-rating` rather than under
        // a weather condition of their own, so the selector's axis for the Wild Area is that one.
        // Recorded because the name reads like a mistake and is not: renaming it here would put this
        // dataset out of step with the source it is regenerated from.
        assertEquals("max-den-rating", conditions.single { it.slug == "weather-sandstorm" }.axis)
    }
}
