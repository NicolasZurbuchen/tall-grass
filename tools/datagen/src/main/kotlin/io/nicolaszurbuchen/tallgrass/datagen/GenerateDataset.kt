package io.nicolaszurbuchen.tallgrass.datagen

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val json =
    Json {
        // The output is read by humans in a pull-request diff before it is read by SQLite.
        prettyPrint = true
        prettyPrintIndent = "  "
        explicitNulls = true
    }

/**
 * Rewrites the committed JSON dataset from the pinned upstream CSVs.
 *
 * Nothing here is time- or environment-dependent: same pin in, byte-identical files out. That is
 * what makes the diff mean something -- a changed line is a changed fact, not churn.
 */
fun main(args: Array<String>) {
    val outputDir = File(args.first()).also { it.mkdirs() }
    val source = UpstreamSource(File(outputDir.parentFile, "build/datagen-cache"))

    println("Generating dataset from ${SOURCE_SHA.take(7)}")

    val types = buildTypeChart(source)
    val species = buildSpecies(source)
    val variants = buildVariants(source, species.map { it.dexNumber }.toSet())
    val abilities = buildAbilities(source)
    val moves = buildMoves(source)
    val learnset = buildLearnset(source, moves.map { it.slug }.toSet(), variants.map { it.slug }.toSet())

    outputDir.resolve("types.json").writeText(json.encodeToString(types))
    outputDir.resolve("species.json").writeText(json.encodeToString(species))
    outputDir.resolve("variants.json").writeText(json.encodeToString(variants))
    outputDir.resolve("abilities.json").writeText(json.encodeToString(abilities))
    outputDir.resolve("moves.json").writeText(json.encodeToString(moves))
    outputDir.resolve("learnset.json").writeText(json.encodeToString(learnset))

    val manifest =
        Manifest(
            schemaVersion = SCHEMA_VERSION,
            sourceSha = SOURCE_SHA,
            speciesCount = species.size,
            variantCount = variants.size,
            listedVariantCount = variants.count { it.listedInDex },
            typeCount = types.types.size,
            abilityCount = abilities.size,
            moveCount = moves.size,
            learnerCount = learnset.sumOf { it.learnedBy.size },
        )
    outputDir.resolve("manifest.json").writeText(json.encodeToString(manifest))

    println("  species            ${manifest.speciesCount}")
    println("  variants           ${manifest.variantCount}")
    println("  listed in the dex  ${manifest.listedVariantCount}")
    println("  types              ${manifest.typeCount}")
    println("  abilities          ${manifest.abilityCount}")
    println("  moves              ${manifest.moveCount}")
    println("  learnset rows      ${manifest.learnerCount}")
}

private fun buildTypeChart(source: UpstreamSource): TypeChartJson {
    val names =
        source.read("type_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("type_id") to it["name"] }

    val realTypes =
        source.read("types")
            .filter { it.int("id") <= LAST_REAL_TYPE_ID }

    val slugById = realTypes.associate { it.int("id") to it["identifier"] }

    val types =
        realTypes
            .map {
                TypeJson(
                    slug = it["identifier"],
                    name = names.getValue(it.int("id")),
                    generation = it.int("generation_id"),
                )
            }.sortedBy { it.slug }

    // Upstream stores all 18x18 pairs including the neutral ones. Only the interesting rows are
    // kept: a missing pair means neutral, which is most of the chart.
    val efficacies =
        source.read("type_efficacy")
            .filter { it.int("damage_factor") != 100 }
            .mapNotNull { row ->
                val damage = slugById[row.int("damage_type_id")] ?: return@mapNotNull null
                val target = slugById[row.int("target_type_id")] ?: return@mapNotNull null
                TypeEfficacyJson(damage, target, row.int("damage_factor"))
            }.sortedWith(compareBy({ it.damage }, { it.target }))

    return TypeChartJson(types, efficacies)
}

private fun buildSpecies(source: UpstreamSource): List<SpeciesJson> {
    val names =
        source.read("pokemon_species_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associateBy { it.int("pokemon_species_id") }

    val growthRates = source.read("growth_rates").associate { it.int("id") to it["identifier"] }
    val eggGroupNames = source.read("egg_groups").associate { it.int("id") to it["identifier"] }

    val eggGroups =
        source.read("pokemon_egg_groups")
            .groupBy({ it.int("species_id") }, { eggGroupNames.getValue(it.int("egg_group_id")) })

    return source.read("pokemon_species")
        .map { row ->
            val id = row.int("id")
            val name = names[id]
            SpeciesJson(
                dexNumber = id,
                slug = row["identifier"],
                name = name?.get("name") ?: row["identifier"],
                genus = name?.get("genus").orEmpty(),
                generation = row.int("generation_id"),
                genderRate = row.int("gender_rate"),
                captureRate = row.int("capture_rate"),
                hatchCounter = row.int("hatch_counter"),
                growthRate = growthRates.getValue(row.int("growth_rate_id")),
                isBaby = row.bool("is_baby"),
                isLegendary = row.bool("is_legendary"),
                isMythical = row.bool("is_mythical"),
                eggGroups = eggGroups[id].orEmpty().sorted(),
            )
        }.sortedBy { it.dexNumber }
}

private fun buildVariants(
    source: UpstreamSource,
    knownSpecies: Set<Int>,
): List<VariantJson> {
    val speciesNames =
        source.read("pokemon_species_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("pokemon_species_id") to it["name"] }

    // The default form of each pokemon carries the name and the form identifier the grid needs. A
    // pokemon with cosmetic-only variety has several form rows and exactly one default.
    val defaultForms =
        source.read("pokemon_forms")
            .filter { it.bool("is_default") }
            .associateBy { it.int("pokemon_id") }

    val formNames =
        source.read("pokemon_form_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associateBy { it.int("pokemon_form_id") }

    val typeSlugs =
        source.read("types")
            .filter { it.int("id") <= LAST_REAL_TYPE_ID }
            .associate { it.int("id") to it["identifier"] }

    val types =
        source.read("pokemon_types")
            .sortedBy { it.int("slot") }
            .groupBy({ it.int("pokemon_id") }, { typeSlugs[it.int("type_id")] })

    val statSlugs =
        source.read("stats")
            .filterNot { it.bool("is_battle_only") }
            .associate { it.int("id") to it["identifier"] }

    val stats =
        source.read("pokemon_stats")
            .groupBy { it.int("pokemon_id") }

    val abilitySlugs = source.read("abilities").associate { it.int("id") to it["identifier"] }

    val abilities =
        source.read("pokemon_abilities")
            .groupBy { it.int("pokemon_id") }

    val speciesSlugs =
        source.read("pokemon_species").associate { it.int("id") to it["identifier"] }

    val drafts =
        source.read("pokemon")
            .filter { it.int("species_id") in knownSpecies }
            .map { row ->
                val id = row.int("id")
                val speciesId = row.int("species_id")
                val defaultForm = defaultForms[id]
                val formName = defaultForm?.let { formNames[it.int("id")] }
                val form = defaultForm?.get("form_identifier").orEmpty()
                val isBattleOnly = defaultForm?.bool("is_battle_only") ?: false

                VariantJson(
                    slug = row["identifier"],
                    speciesDexNumber = speciesId,
                    speciesSlug = speciesSlugs[speciesId].orEmpty(),
                    name = formName?.get("pokemon_name").orEmpty().ifEmpty { speciesNames[speciesId] ?: row["identifier"] },
                    formLabel = formName?.get("form_name").orEmpty().ifEmpty { null },
                    form = form.ifEmpty { null },
                    // Replaced in the second pass below: classifying a form needs its species'
                    // default variant, which does not exist yet while this one is being built.
                    formKind = FormKind.NONE,
                    isMega = defaultForm?.bool("is_mega") ?: false,
                    isBattleOnly = isBattleOnly,
                    isDefault = row.bool("is_default"),
                    // One card per species, and no more: the browse list is the National Dex.
                    // DECISIONS.md § The dex lists one card per species, forms behind it
                    listedInDex = row.bool("is_default"),
                    height = row.int("height"),
                    weight = row.int("weight"),
                    artworkUrl = artworkUrl(id),
                    // The upstream `order` column would be the obvious choice and is empty for 139
                    // rows, most of Generation VIII and IX among them. The id works instead because
                    // of how upstream allocates it: an ordinary form keeps its Dex number and a
                    // non-default form is numbered from 10000, so within a species the base form
                    // leads and its variants follow.
                    sortOrder = id,
                    types = types[id].orEmpty().filterNotNull(),
                    stats =
                        stats[id].orEmpty()
                            .mapNotNull { stat -> statSlugs[stat.int("stat_id")]?.let { it to stat.int("base_stat") } }
                            .sortedBy { it.first }
                            .toMap(),
                    abilities =
                        abilities[id].orEmpty()
                            .mapNotNull { ability ->
                                val abilityId = ability.int("ability_id")
                                val slug = abilitySlugs[abilityId] ?: return@mapNotNull null
                                AbilityRefJson(
                                    slug = slug,
                                    isHidden = ability.bool("is_hidden"),
                                    slot = ability.int("slot"),
                                )
                            }.sortedBy { it.slot },
                )
            }

    // Second pass. A form is cosmetic when it is indistinguishable from its species' default form,
    // so every default form has to exist before anything can be classified.
    val defaults = drafts.filter { it.isDefault }.associateBy { it.speciesDexNumber }

    val classified =
        drafts.map { draft ->
            val base = defaults[draft.speciesDexNumber]
            draft.copy(
                formKind =
                    classifyForm(
                        isDefault = draft.isDefault,
                        form = draft.form.orEmpty(),
                        isMega = draft.isMega,
                        isBattleOnly = draft.isBattleOnly,
                        differsFromBase = base == null || !draft.sharesBattleDataWith(base),
                    ),
            )
        }

    return (classified + typeChangingForms(source, defaults))
        .sortedWith(compareBy({ it.speciesDexNumber }, { it.sortOrder }, { it.slug }))
}

/**
 * Arceus's Plates and Silvally's Memories, which upstream models as forms rather than Pokemon.
 *
 * They get no `pokemon` row because their stats, abilities and moves are identical to the base form
 * -- **only the type moves**, and `pokemon_form_types.csv` holds exactly 35 rows, all of them these
 * two. Left out, the dataset says `arceus [normal]` and seventeen of its eighteen forms are
 * unrepresentable, which would have the Stats tab report Fire Arceus as weak to Fighting.
 *
 * They are promoted here because the test for a variant is **battle-relevance, not different
 * stats** -- the same rule that makes Gigantamax and Meowstic F variants despite carrying their base
 * form's stats exactly. A type change is as battle-relevant as it gets.
 *
 * Everything except the type and the artwork is inherited from the base form, because upstream has
 * nothing else to give: there is no per-form stat, ability or breeding table.
 */
private fun typeChangingForms(
    source: UpstreamSource,
    defaults: Map<Int, VariantJson>,
): List<VariantJson> {
    val typeSlugs =
        source.read("types")
            .filter { it.int("id") <= LAST_REAL_TYPE_ID }
            .associate { it.int("id") to it["identifier"] }

    val typesByForm =
        source.read("pokemon_form_types")
            .sortedBy { it.int("slot") }
            .groupBy({ it.int("pokemon_form_id") }, { typeSlugs[it.int("type_id")] })

    val formNames =
        source.read("pokemon_form_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associateBy { it.int("pokemon_form_id") }

    val speciesByPokemon =
        source.read("pokemon").associate { it.int("id") to it.int("species_id") }

    return source.read("pokemon_forms")
        .filterNot { it.bool("is_default") }
        .mapNotNull { form ->
            val formId = form.int("id")
            val types = typesByForm[formId]?.filterNotNull().orEmpty()
            if (types.isEmpty()) return@mapNotNull null

            val pokemonId = form.int("pokemon_id")
            val speciesId = speciesByPokemon[pokemonId] ?: return@mapNotNull null
            val base = defaults[speciesId] ?: return@mapNotNull null
            val formName = formNames[formId]
            val formIdentifier = form["form_identifier"].ifEmpty { null }

            base.copy(
                slug = form["identifier"],
                name = formName?.get("pokemon_name").orEmpty().ifEmpty { base.name },
                formLabel = formName?.get("form_name").orEmpty().ifEmpty { null },
                form = formIdentifier,
                // Battle-relevant, and none of the earlier buckets name it.
                formKind = FormKind.ALTERNATE,
                isDefault = false,
                // Eighteen Arceus in the grid would bury the other 1081 cards.
                listedInDex = false,
                types = types,
                // Named, not numbered. These forms have no `pokemon` row, so there is no id that
                // stands for them in the artwork set -- and the form id, which does exist, indexes
                // a different table: `artworkUrl(formId)` handed Dragon Arceus a picture of Mega
                // Mewtwo X, because 10043 is a real pokemon id belonging to someone else.
                artworkUrl =
                    formIdentifier
                        ?.let { formArtworkUrl(pokemonId, it) }
                        ?: base.artworkUrl,
                sortOrder = formId,
            )
        }
}

/**
 * Whether two variants are indistinguishable in everything this dataset models about a battle.
 *
 * **Abilities are part of the comparison and cannot be left out.** Eight forms differ from their
 * base by ability alone -- `greninja-battle-bond`, `rockruff-own-tempo`, `toxtricity-low-key`,
 * `zygarde-50-power-construct`, `basculin-blue-striped` and two Squawkabilly plumages -- and a
 * stats-and-types comparison files all of them as costumes.
 *
 * Moves are not compared because they are not ingested yet. That makes `keldeo-resolute` read as
 * cosmetic when it really differs by learning Secret Sword; it stops being wrong when moves land.
 */
private fun VariantJson.sharesBattleDataWith(other: VariantJson): Boolean =
    stats == other.stats &&
        types == other.types &&
        abilities.map { it.slug }.sorted() == other.abilities.map { it.slug }.sorted()

/**
 * The 314 main-series abilities, categorised.
 *
 * Upstream numbers Pokemon Conquest's sixty abilities from 10000 in the same table and flags them
 * `is_main_series = 0`. Both tests are applied rather than either alone, because they are two
 * different claims: one is upstream's own judgement and the other is its id convention. None of the
 * sixty has effect text in any language and none is on any Pokemon, so a card for Mountaineer would
 * be a name over an empty space with an empty "known by" underneath.
 */
private fun buildAbilities(source: UpstreamSource): List<AbilityJson> {
    val names =
        source.read("ability_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("ability_id") to it["name"] }

    val prose =
        source.read("ability_prose")
            .filter { it.int("local_language_id") == ENGLISH }
            .associateBy { it.int("ability_id") }

    return source.read("abilities")
        .filter { it.bool("is_main_series") && it.int("id") < FIRST_SPIN_OFF_ID }
        .map { row ->
            val id = row.int("id")
            val slug = row["identifier"]

            // Fails the run rather than shipping a blank card. All 314 have one today, so an absence
            // means upstream added an ability this pipeline has not been taught about -- and this
            // program only runs when a human deliberately bumps the pin, which is the moment to
            // notice. Moves are the opposite case and are nullable; see below.
            val entry = prose[id] ?: error("No English effect entry for ability '$slug' ($id)")
            val shortEffect = entry["short_effect"]

            AbilityJson(
                slug = slug,
                name = names[id] ?: slug,
                generation = row.int("generation_id"),
                shortEffect = shortEffect,
                // Paragraphs, and upstream writes them with a blank line between. Collapsed to one
                // newline so the screen decides the spacing rather than inheriting a wiki's.
                effect = entry["effect"].replace(BLANK_LINE, "\n").trim(),
            )
        }.sortedBy { it.slug }
}

/** Upstream separates paragraphs with a blank line, sometimes several. */
private val BLANK_LINE = Regex("""\n\s*\n+""")

/** Upstream's identifier for ailment id 0, which 703 of the 827 moves with a meta row carry. */
private const val NO_AILMENT = "none"

/**
 * The 919 main-series moves.
 *
 * Same threshold and the same reason as the abilities above: ids from 10000 are Pokemon XD's
 * eighteen Shadow moves, which exist in one 2005 spin-off.
 *
 * **93 of these have no effect text**, all of them Generation VIII and IX -- Tera Blast, Ice Spinner,
 * Salt Cure, Last Respects. They carry no `effect_id` at all rather than one whose English row is
 * missing, so the column is empty and not merely unjoinable. They ship with a null and the screen
 * shows the space as empty, which is the honest rendering of "upstream does not say".
 */
private fun buildMoves(source: UpstreamSource): List<MoveJson> {
    val names =
        source.read("move_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("move_id") to it["name"] }

    // Keyed by effect rather than by move: 453 effects cover 919 moves, because every move that
    // "inflicts regular damage with no additional effect" shares one row.
    val prose =
        source.read("move_effect_prose")
            .filter { it.int("local_language_id") == ENGLISH }
            .associateBy { it.int("move_effect_id") }

    val typeSlugs =
        source.read("types")
            .filter { it.int("id") <= LAST_REAL_TYPE_ID }
            .associate { it.int("id") to it["identifier"] }

    val damageClasses = source.read("move_damage_classes").associate { it.int("id") to it["identifier"] }
    val targets = source.read("move_targets").associate { it.int("id") to it["identifier"] }

    val categories = source.read("move_meta_categories").associate { it.int("id") to it["identifier"] }
    val ailments = source.read("move_meta_ailments").associate { it.int("id") to it["identifier"] }
    val meta = source.read("move_meta").associateBy { it.int("move_id") }

    val statSlugs = source.read("stats").associate { it.int("id") to it["identifier"] }
    val statChanges =
        source.read("move_meta_stat_changes")
            .groupBy { it.int("move_id") }
            .mapValues { (_, rows) ->
                // Sorted by upstream's stat id, which is the order the stat bars are already drawn
                // in. The table has no ordering column of its own.
                rows.sortedBy { it.int("stat_id") }
                    .associate { statSlugs.getValue(it.int("stat_id")) to it.int("change") }
            }

    return source.read("moves")
        .filter { it.int("id") < FIRST_SPIN_OFF_ID }
        .map { row ->
            val id = row.int("id")
            val slug = row["identifier"]
            val entry = row.intOrNull("effect_id")?.let { prose[it] }

            MoveJson(
                slug = slug,
                name = names[row.int("id")] ?: slug,
                generation = row.int("generation_id"),
                type = typeSlugs[row.int("type_id")] ?: error("Move '$slug' has an unknown type"),
                damageClass = damageClasses[row.int("damage_class_id")] ?: error("Move '$slug' has an unknown damage class"),
                power = row.intOrNull("power"),
                accuracy = row.intOrNull("accuracy"),
                pp = row.intOrNull("pp"),
                priority = row.int("priority"),
                target = targets[row.int("target_id")] ?: error("Move '$slug' has an unknown target"),
                shortEffect = entry?.get("short_effect"),
                effect = entry?.get("effect")?.replace(BLANK_LINE, "\n")?.trim(),
                meta = meta[id]?.let { buildMoveMeta(slug, it, categories, ailments) },
                statChanges = statChanges[id].orEmpty(),
            )
        }.sortedBy { it.slug }
}

/**
 * Turns one `move_meta` row into [MoveMetaJson], dropping every number that only restates a default.
 *
 * Which zeroes are facts is not in the row -- it is in whether a neighbouring field is set. See
 * `DECISIONS.md § A move's mechanical detail is null where there is nothing to say`.
 */
private fun buildMoveMeta(
    slug: String,
    row: CsvRow,
    categories: Map<Int, String>,
    ailments: Map<Int, String>,
): MoveMetaJson {
    val ailment = ailments[row.int("meta_ailment_id")]?.takeIf { it != NO_AILMENT }

    return MoveMetaJson(
        category = categories[row.int("meta_category_id")] ?: error("Move '$slug' has an unknown meta category"),
        ailment = ailment,
        // Five moves carry a chance with no ailment for it to be a chance of -- Frost Breath stores
        // 100. A percentage naming no effect cannot be drawn, so it leaves with the ailment.
        ailmentChance = row.int("ailment_chance").takeIf { it != 0 && ailment != null },
        minHits = row.intOrNull("min_hits"),
        maxHits = row.intOrNull("max_hits"),
        minTurns = row.intOrNull("min_turns"),
        maxTurns = row.intOrNull("max_turns"),
        drain = row.int("drain").takeIf { it != 0 },
        healing = row.int("healing").takeIf { it != 0 },
        critRate = row.int("crit_rate").takeIf { it != 0 },
        flinchChance = row.int("flinch_chance").takeIf { it != 0 },
        statChance = row.int("stat_chance").takeIf { it != 0 },
    )
}

/**
 * Which Pokemon learn each move, from each Pokemon's most recent appearance.
 *
 * `pokemon_moves` is 638,321 rows because it holds every version group a Pokemon has ever been in.
 * Filtering each Pokemon to the highest version group it appears in leaves 71,940, which is #7's
 * latest-by-default applied to a table rather than to a screen: what a Pokemon learns is what it
 * learns in the newest game that has it.
 *
 * **One row per Pokemon and move, not one per way of getting it.** A move that is both a level-up
 * move and a TM is one fact on a card, and upstream files it twice; [METHOD_PRIORITY] picks which
 * answer to keep, lowest level first where there are several. 71,940 rows become 46,679.
 *
 * `train` is excluded and is not a way of learning anything -- it is Legends: Arceus's move mastery,
 * which sharpens a move the Pokemon already has. Left in, it would be 19,810 rows claiming a Pokemon
 * learns by training.
 */
private fun buildLearnset(
    source: UpstreamSource,
    knownMoves: Set<String>,
    knownVariants: Set<String>,
): List<LearnsetJson> {
    val methods = source.read("pokemon_move_methods").associate { it.int("id") to it["identifier"] }
    val moveSlugs = source.read("moves").associate { it.int("id") to it["identifier"] }
    val variantSlugs = source.read("pokemon").associate { it.int("id") to it["identifier"] }

    // **Filtered to real methods before anything asks which version group is newest**, and the order
    // is the whole rule rather than a tidying. Version group 32 is Pokemon Champions, where every
    // row is `train`; taken as a Pokemon's newest appearance it leaves 319 of them -- Charizard among
    // them -- with an empty learnset, because mastery is all that game records.
    //
    // What this asks instead is "the newest game in which it actually learns something", which needs
    // no list of titles to skip and answers the same way for whatever upstream adds next.
    val rows =
        source.read("pokemon_moves")
            .filter { methods[it.int("pokemon_move_method_id")] in METHOD_PRIORITY }

    // Highest wins because upstream allocates version group ids in release order, which is the only
    // ordering it publishes. A generation added out of order upstream would need a real table here.
    val newestPerPokemon =
        rows.groupBy { it.int("pokemon_id") }
            .mapValues { (_, entries) -> entries.maxOf { it.int("version_group_id") } }

    val best = mutableMapOf<Pair<String, String>, LearnerJson>()

    rows.forEach { row ->
        val pokemonId = row.int("pokemon_id")
        if (row.int("version_group_id") != newestPerPokemon[pokemonId]) return@forEach

        val variant = variantSlugs[pokemonId]?.takeIf { it in knownVariants } ?: return@forEach
        val move = moveSlugs[row.int("move_id")]?.takeIf { it in knownMoves } ?: return@forEach
        val method = methods[row.int("pokemon_move_method_id")]?.takeIf { it in METHOD_PRIORITY } ?: return@forEach

        // **Zero is not a level.** Upstream writes 0 in this column for every machine, egg and tutor
        // row -- 33,677 of the 46,679 -- where the question does not arise, and for the 160 level-up
        // moves a Pokemon knows without being taught, which it learns on evolution or already has.
        // Carried through, a TM would say the move is learned at level 0.
        val level = row.intOrNull("level")?.takeIf { method == LEVEL_UP && it > 0 }

        val candidate = LearnerJson(variant = variant, method = method, level = level)
        val existing = best[move to variant]

        if (existing == null || candidate.beats(existing)) best[move to variant] = candidate
    }

    return best.entries
        .groupBy({ it.key.first }, { it.value })
        .let { byMove ->
            knownMoves.sorted().map { move ->
                LearnsetJson(
                    slug = move,
                    learnedBy = byMove[move].orEmpty().sortedWith(compareBy({ it.method }, { it.variant })),
                )
            }
        }
}

/**
 * Earlier in [METHOD_PRIORITY] wins; between two level-up rows, the earlier one does.
 *
 * No level counts as earlier than any level, because for a level-up row that is what it means: the
 * Pokemon knows the move without being taught it.
 */
private fun LearnerJson.beats(other: LearnerJson): Boolean {
    val rank = METHOD_PRIORITY.indexOf(method)
    val otherRank = METHOD_PRIORITY.indexOf(other.method)

    if (rank != otherRank) return rank < otherRank

    return (level ?: 0) < (other.level ?: 0)
}

/**
 * Which answer to keep when a Pokemon has more than one way to the same move, best first.
 *
 * Level-up leads because it is the one that carries a number and so says the most; a TM says only
 * that it is possible. The order is a judgement about what a card should show rather than a fact
 * about the games, which is why it is here rather than in the domain.
 */
private val METHOD_PRIORITY = listOf(LEVEL_UP, "machine", "egg", "tutor")

/** The one method that carries a number, which is why it is named rather than spelled twice. */
private const val LEVEL_UP = "level-up"
