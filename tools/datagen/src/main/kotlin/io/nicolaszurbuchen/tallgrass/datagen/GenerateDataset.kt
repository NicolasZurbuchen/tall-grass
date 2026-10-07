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

/** For the two hand-written inputs, whose `comment` blocks are prose for a reader rather than data. */
private val inputJson = Json { ignoreUnknownKeys = true }

/**
 * Reads one of this generator's own committed inputs.
 *
 * The only two files here that are not read from the pinned upstream. Both say inside themselves
 * where they came from and why they have to exist at all.
 */
private inline fun <reified T> readInput(name: String): T {
    val text =
        GmaxCatalogueJson::class.java.getResourceAsStream(name)?.bufferedReader()?.readText()
            ?: error("Missing generator input: $name")

    return inputJson.decodeFromString(text)
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

    val gmax = readInput<GmaxCatalogueJson>("/gmax-moves.json")
    val maxPower = readInput<MaxPowerJson>("/max-move-power.json")

    val types = buildTypeChart(source)
    val species = buildSpecies(source)
    val variants = buildVariants(source, species.map { it.dexNumber }.toSet())
    val abilities = buildAbilities(source)

    // The G-Max Moves join the list before the learnset is built, so they are known moves like any
    // other and get an entry of their own -- an empty one, because nothing learns a G-Max Move.
    val moves = (buildMoves(source) + gmax.toMovesJson()).sortedBy { it.slug }
    val learnset =
        buildLearnset(source, moves.map { it.slug }.toSet(), variants.map { it.slug }.toSet())
            .withBorrowedForms(variants)
            .withGmaxLearners(gmax)

    val gameVersions = buildVersions(source)
    val places = buildLocations(source)
    val harvest =
        buildEncounters(
            source = source,
            versions = gameVersions,
            knownVariants = variants.map { it.slug }.toSet(),
            regionByLocation = places.associate { it.slug to it.region },
        )
    val locations = places.withEncounteredVersions(harvest.regions)
    val regions = buildRegions(source, gameVersions, locations)
    val conditions = buildEncounterConditions(source, harvest.referencedConditions)

    outputDir.resolve("types.json").writeText(json.encodeToString(types))
    outputDir.resolve("species.json").writeText(json.encodeToString(species))
    outputDir.resolve("variants.json").writeText(json.encodeToString(variants))
    outputDir.resolve("abilities.json").writeText(json.encodeToString(abilities))
    outputDir.resolve("moves.json").writeText(json.encodeToString(moves.withMaxPower(learnset, maxPower)))
    outputDir.resolve("learnset.json").writeText(json.encodeToString(learnset))
    outputDir.resolve("versions.json").writeText(json.encodeToString(gameVersions))
    outputDir.resolve("regions.json").writeText(json.encodeToString(regions))
    outputDir.resolve("locations.json").writeText(json.encodeToString(locations))
    outputDir.resolve("encounter-conditions.json").writeText(json.encodeToString(conditions))

    // One file per region rather than one of 28 MB. Written into their own directory because eleven
    // more files beside seven would bury the seven.
    val encounterDir = outputDir.resolve("encounters").also { it.mkdirs() }
    encounterDir.listFiles()?.forEach { it.delete() }
    harvest.regions.forEach { region ->
        encounterDir.resolve("${region.region}.json").writeText(json.encodeToString(region))
    }

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
            regionCount = regions.size,
            locationCount = locations.size,
            versionCount = gameVersions.size,
            encounterSlotCount = harvest.regions.sumOf { region -> region.locations.sumOf { it.slotCount() } },
        )
    outputDir.resolve("manifest.json").writeText(json.encodeToString(manifest))

    println("  species            ${manifest.speciesCount}")
    println("  variants           ${manifest.variantCount}")
    println("  listed in the dex  ${manifest.listedVariantCount}")
    println("  types              ${manifest.typeCount}")
    println("  abilities          ${manifest.abilityCount}")
    println("  moves              ${manifest.moveCount}")
    println("  learnset rows      ${manifest.learnerCount}")
    println("  regions            ${manifest.regionCount}")
    println("  locations          ${manifest.locationCount}")
    println("  versions           ${manifest.versionCount}")
    println("  encounter slots    ${manifest.encounterSlotCount}")
    println("  rows not placed    ${harvest.dropped}")
}

private fun LocationEncountersJson.slotCount(): Int = versions.sumOf { version -> version.tables.sumOf { it.slots.size } }

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
                    isBattleOnly = isBattleOnly,
                    isDefault = row.bool("is_default"),
                    // One card per species, and no more: the browse list is the National Dex.
                    // DECISIONS.md § The dex lists one card per species, forms behind it
                    listedInDex = row.bool("is_default"),
                    height = row.int("height"),
                    weight = row.int("weight"),
                    // Empty for 49 rows, all of them Legends Z-A Megas upstream has not costed yet.
                    baseExperience = row.intOrNull("base_experience"),
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
                    // The same six rows read for their other column. Only what the form actually
                    // awards is kept, so a form upstream has not costed comes out empty rather than
                    // claiming to yield nothing against all six.
                    evYield =
                        stats[id].orEmpty()
                            .filter { stat -> stat.int("effort") > 0 }
                            .mapNotNull { stat -> statSlugs[stat.int("stat_id")]?.let { it to stat.int("effort") } }
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

    // **Upstream's own flag, read here rather than carried on [VariantJson].** It was a field until
    // it was noticed that nothing read it: `formKind` is the derived answer, and all 97 rows it is
    // true for come out MEGA. Only the classifier below ever wanted it.
    //
    // The alternative is `form.startsWith("mega")`, which is true of exactly the same 97 today and
    // is still a convention where this is a fact. Primal Groudon is the reminder that the two can
    // come apart: the same kind of thing to a player, and upstream does not flag it.
    val megaSlugs =
        source.read("pokemon")
            .filter { defaultForms[it.int("id")]?.bool("is_mega") == true }
            .map { it["identifier"] }
            .toSet()

    val classified =
        drafts.map { draft ->
            val base = defaults[draft.speciesDexNumber]
            draft.copy(
                formKind =
                    classifyForm(
                        isDefault = draft.isDefault,
                        form = draft.form.orEmpty(),
                        isMega = draft.slug in megaSlugs,
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
                // Filled by [withMaxPower], which cannot run until the learnset exists.
                maxPower = null,
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
 * The hand-written G-Max Moves as ordinary moves, so each one is a row a reader can open.
 *
 * Most of what a move row holds cannot be stated for one of these. The category and the power come
 * from whichever move was replaced, so they are not facts about the G-Max Move at all -- except for
 * the three that are 160 whatever they replaced. [MoveJson.damageClass] is not nullable and cannot
 * be left out, so these carry the same nominal "physical" that upstream files its own nineteen Max
 * Moves under; nothing reads it, because the conversion takes the category from the base move.
 */
private fun GmaxCatalogueJson.toMovesJson(): List<MoveJson> =
    moves.map { move ->
        MoveJson(
            slug = move.slug,
            name = move.name,
            generation = DYNAMAX_GENERATION,
            type = move.type,
            damageClass = NOMINAL_DAMAGE_CLASS,
            power = move.power,
            // A Max Move cannot miss, and upstream's own nineteen are filed the same way.
            accuracy = null,
            pp = MAX_MOVE_PP,
            priority = 0,
            target = MAX_MOVE_TARGET,
            // One sentence is the whole of what there is to say, and the detail screen reads the long
            // field while a row reads the short one. Splitting it would mean writing it twice.
            shortEffect = move.effect,
            effect = move.effect,
            maxPower = null,
            meta = null,
            statChanges = emptyMap(),
        )
    }

/**
 * Gives each G-Max Move the forms that have it.
 *
 * **Which Pokemon has which move is the learnset's question**, and it was being answered twice: once
 * here and once by a `gmaxMove` column on the variant. The column went, because this is the table
 * that already joins the two and the G-Max Moves were sitting in it with an empty `learnedBy` -- a
 * row saying nothing has G-Max Vine Lash, next to a column saying Gigantamax Venusaur does.
 *
 * It also fixes the move's own screen, which had nothing to list under Learned by.
 *
 * **Runs after [withBorrowedForms]**, which drops every row belonging to a Mega or a Gigantamax
 * before putting the base form's back. Run the other way round and these would be among what it
 * dropped.
 */
private fun List<LearnsetJson>.withGmaxLearners(catalogue: GmaxCatalogueJson): List<LearnsetJson> {
    val formsOf = catalogue.moves.associate { it.slug to it.forms }

    return map { entry ->
        val forms = formsOf[entry.slug] ?: return@map entry

        entry.copy(learnedBy = forms.sorted().map { LearnerJson(variant = it, method = GIGANTAMAX, level = null) })
    }
}

/**
 * What each move's power becomes when it is Dynamaxed.
 *
 * **Not a scaling of the move's own power but a step function of it**, seven bands wide, with a
 * lower set of steps for Fighting and Poison. The bands cover 386 of the 439 moves that can be
 * Dynamaxed; `max-move-power.json` holds the other 53 and says why each is there.
 *
 * Null for the status moves, which all become Max Guard and have no power to have, and null for
 * everything nothing learns. That last test is what keeps the Z-Moves out: Catastropika is power 210
 * and the bands would happily call it 150, but it is not a move any Pokemon has -- it is a move a
 * Z-Crystal makes out of one -- and the question does not arise for it. Same for the Max Moves
 * themselves, which are what this produces rather than what it reads.
 */
private fun List<MoveJson>.withMaxPower(
    learnset: List<LearnsetJson>,
    exceptions: MaxPowerJson,
): List<MoveJson> {
    val learned = learnset.filter { it.learnedBy.isNotEmpty() }.map { it.slug }.toSet()

    return map { move ->
        val power =
            when {
                // What this produces rather than what it reads. A Max Move cannot itself be
                // Dynamaxed, and the three G-Max Moves with a power of their own would otherwise be
                // handed a second one.
                move.isMaxMove() -> null

                // Not a move any Pokemon has. The Z-Moves are what this catches: Catastropika is
                // power 210 and the bands would happily call it 150, but it is a move a Z-Crystal
                // makes out of another rather than one a Pokemon learns.
                move.slug !in learned -> null

                // Ahead of the bands rather than after them, because most of these have no power for
                // a band to read in the first place.
                move.slug in exceptions.power -> exceptions.power.getValue(move.slug)

                move.damageClass == STATUS_DAMAGE_CLASS -> null

                else -> move.power?.let { base -> bracketsFor(move.type).first { base <= it.first }.second }
            }

        move.copy(maxPower = power)
    }
}

/**
 * Whether this move is one of the fifty-two a Dynamaxed Pokemon's moves turn into.
 *
 * The two prefixes rather than one, because `g-max-` does not start with `max-`.
 */
private fun MoveJson.isMaxMove(): Boolean = slug.startsWith(MAX_MOVE_PREFIX) || slug.startsWith(GMAX_MOVE_PREFIX)

private fun bracketsFor(typeSlug: String): List<Pair<Int, Int>> =
    if (typeSlug in WEAKENED_MAX_TYPES) WEAKENED_MAX_BRACKETS else MAX_BRACKETS

/**
 * The upper bound of each band and what a move in it becomes, lowest first. Read in order, so the
 * first band a move fits is its own.
 *
 * The bands are uneven on purpose -- 75 through 100 is one step, which is why most of a Pokemon's
 * serious attacking moves come out of the conversion at the same power and why the converted list is
 * so much shorter than the one it was made from.
 */
private val MAX_BRACKETS =
    listOf(40 to 90, 50 to 100, 60 to 110, 70 to 120, 100 to 130, 140 to 140, Int.MAX_VALUE to 150)

/** The same seven bands about a third weaker, which is a balance decision in the games. */
private val WEAKENED_MAX_BRACKETS =
    listOf(40 to 70, 50 to 75, 60 to 80, 70 to 85, 100 to 90, 140 to 95, Int.MAX_VALUE to 100)

private val WEAKENED_MAX_TYPES = setOf("fighting", "poison")

private const val STATUS_DAMAGE_CLASS = "status"

private const val MAX_MOVE_PREFIX = "max-"
private const val GMAX_MOVE_PREFIX = "g-max-"

/**
 * Not one of upstream's four methods and not a way of learning anything: a Gigantamax form has its
 * G-Max Move by being that form, the way a Pokemon has its types.
 *
 * It is a learnset row all the same, because the question "which Pokemon has this move" has one
 * table and this is it.
 */
private const val GIGANTAMAX = "gigantamax"

/** Sword and Shield, where Dynamax was introduced and where it has stayed. */
private const val DYNAMAX_GENERATION = 8

/**
 * What upstream files its own nineteen Max Moves under, matched so the fifty-two read alike.
 *
 * Nominal in both cases: a Max Move's category is the category of the move it replaced.
 */
private const val NOMINAL_DAMAGE_CLASS = "physical"

private const val MAX_MOVE_PP = 10

/** Upstream's target vocabulary, and the value its own Max Moves carry. */
private const val MAX_MOVE_TARGET = "selected-pokemon-me-first"

/**
 * Which Pokemon learn each move, from each Pokemon's most recent appearance.
 *
 * `pokemon_moves` is 638,321 rows because it holds every version group a Pokemon has ever been in.
 * Filtering each Pokemon to the newest version group it learns anything in leaves 74,414, which is #7's
 * latest-by-default applied to a table rather than to a screen: what a Pokemon learns is what it
 * learns in the newest game that has it.
 *
 * **One row per Pokemon and move, not one per way of getting it.** A move that is both a level-up
 * move and a TM is one fact on a card, and upstream files it twice; [METHOD_PRIORITY] picks which
 * answer to keep, lowest level first where there are several. 74,414 rows become 66,553.
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

    // **Upstream's own ordering column, not its ids.** This asked for the highest id, on the reasoning
    // that a version group is numbered when its games come out -- which held until `red-green-japan`
    // and `blue-japan` were added at 28 and 29, behind Scarlet/Violet at 25. Every Pokemon in the
    // 1996 Japanese carts then read as though 1996 were the newest game that had it: Charizard learnt
    // Flamethrower at 46 and knew Bide, Rage and Submission, and 151 of them were wrong together.
    //
    // `order` is what upstream publishes for exactly this question, and what [buildVersions] four
    // hundred lines down already sorts the game grid on.
    val releaseOrder = source.read("version_groups").associate { it.int("id") to it.int("order") }

    val newestPerPokemon =
        rows.groupBy { it.int("pokemon_id") }
            .mapValues { (_, entries) -> entries.maxOf { releaseOrder.getValue(it.int("version_group_id")) } }

    val best = mutableMapOf<Pair<String, String>, LearnerJson>()

    rows.forEach { row ->
        val pokemonId = row.int("pokemon_id")
        if (releaseOrder.getValue(row.int("version_group_id")) != newestPerPokemon[pokemonId]) return@forEach

        val variant = variantSlugs[pokemonId]?.takeIf { it in knownVariants } ?: return@forEach
        val move = moveSlugs[row.int("move_id")]?.takeIf { it in knownMoves } ?: return@forEach
        val method = methods[row.int("pokemon_move_method_id")]?.takeIf { it in METHOD_PRIORITY } ?: return@forEach

        // **Zero is not a level.** Upstream writes 0 in this column for every machine, egg and tutor
        // row -- 48,163 of the 66,553 -- where the question does not arise, and for the 322 level-up
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
 * Gives a form with no learnset of its own the moves of the form it is a state of.
 *
 * **Two kinds of form qualify, and not for the same reason.**
 *
 * A Mega Evolution happens mid-battle and the Pokemon fights on with the four moves it came in with;
 * a Gigantamax is the same kind of thing. Neither is a Pokemon that learnt anything different, so
 * neither has a learnset of its own to have -- and upstream files them as `pokemon` rows regardless,
 * which quietly turns their learnset into a fact about *which games the form appears in*. Charizard
 * is read from Scarlet/Violet and Mega Charizard X from Let's Go, because Let's Go is the newest game
 * a Mega Charizard exists in, so the detail screen's form switcher appeared to rewrite the move list.
 *
 * The other kind is a form upstream files no row for at all: the 34 Gigantamaxes, and Arceus's and
 * Silvally's seventeen type forms each, where the plate or the memory is carried rather than learnt.
 * Nothing can be wrong with rows that do not exist, and what was wrong is the answer they produced --
 * a Pokemon that knows nothing. **Stated as a rule about missing rows rather than about those two
 * species**, so a cosmetic form added upstream inherits instead of arriving empty.
 *
 * The base form is whatever the slug says before the word that makes it one of these, which is not
 * the same as the species' default form: `meowstic-female-mega` borrows from `meowstic-female`, and
 * the default is the male, which genuinely learns a different set. Where the slug leaves nothing
 * behind -- `pyroar-mega`, whose base is filed as `pyroar-male`, `zygarde-mega`, whose is
 * `zygarde-50`, and every type form, whose slug carries no such word at all -- the default is the
 * answer after all.
 */
private fun List<LearnsetJson>.withBorrowedForms(variants: List<VariantJson>): List<LearnsetJson> {
    val known = variants.map { it.slug }.toSet()
    val defaults = variants.filter { it.isDefault }.associate { it.speciesSlug to it.slug }
    val taught = flatMap { it.learnedBy }.map { it.variant }.toSet()

    // The second clause is the general rule and the first is the exception to it: a Mega or a
    // Gigantamax borrows even where upstream gave it rows, because those rows are wrong rather than
    // incomplete.
    //
    // A default form is never a borrower. One with no rows has nothing to borrow from either, and is
    // a gap worth failing a test over rather than papering over.
    val borrowedFrom =
        variants.filter { it.formKind in BORROWING_FORM_KINDS || (!it.isDefault && it.slug !in taught) }
            .associate { variant ->
                val base = variant.slug.baseFormSlug()?.takeIf { it in known }

                variant.slug to (base ?: defaults.getValue(variant.speciesSlug))
            }

    // Inverted once rather than searched per learner: this runs against 66,553 of them.
    val borrowersOf = borrowedFrom.entries.groupBy({ it.value }, { it.key })

    return map { entry ->
        // **Dropped, not merged.** A Mega with rows of its own has the wrong rows, not a subset of
        // the right ones -- that is the whole of what this function is here to correct.
        val own = entry.learnedBy.filterNot { it.variant in borrowedFrom }
        val borrowed = own.flatMap { learner -> borrowersOf[learner.variant].orEmpty().map { learner.copy(variant = it) } }

        entry.copy(learnedBy = (own + borrowed).sortedWith(compareBy({ it.method }, { it.variant })))
    }
}

/**
 * Everything before the word that makes a slug a Mega or a Gigantamax, or null when it has no such
 * word.
 *
 * Read off the slug rather than off upstream's `form` identifier, which does not reliably line up
 * with it: `meowstic-male-mega` is filed under the form `mega-male`, and stripping that leaves
 * nothing to look up.
 */
private fun String.baseFormSlug(): String? {
    val parts = split('-')
    val marker = parts.indexOfLast { it in FORM_MARKERS }

    return if (marker > 0) parts.take(marker).joinToString("-") else null
}

/**
 * The two that borrow whatever upstream gave them, because their moves are their base form's by the
 * rules of the game rather than by a gap in the data.
 */
private val BORROWING_FORM_KINDS = setOf(FormKind.MEGA, FormKind.GIGANTAMAX)

/** `mega` covers `-mega`, `-mega-x`, `-mega-y` and Legends Z-A's `-mega-z` in one word. */
private val FORM_MARKERS = setOf("mega", "gmax")

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

/**
 * Every version the app draws a cell for.
 *
 * Ordered newest console first and, within a console, oldest game first -- which is how the grid
 * reads: the row a player is most likely to be holding is at the top, and the games inside it run in
 * the order they came out.
 */
private fun buildVersions(source: UpstreamSource): List<VersionJson> {
    val names =
        source.read("version_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("version_id") to it["name"] }

    val groups = source.read("version_groups").associateBy { it.int("id") }

    val regionsByGroup =
        source.read("version_group_regions")
            .groupBy({ it.int("version_group_id") }, { it.int("region_id") })

    val regionSlugs = source.read("regions").associate { it.int("id") to it["identifier"] }

    return source.read("versions")
        .filter { isShippedVersion(it["identifier"]) }
        .map { row ->
            val group = groups.getValue(row.int("version_group_id"))
            val generation = group.int("generation_id")
            VersionJson(
                slug = row["identifier"],
                name = names[row.int("id")] ?: row["identifier"],
                code = versionCodeOf(row["identifier"]),
                console = consoleOf(group["identifier"], generation),
                generation = generation,
                versionGroup = group["identifier"],
                regions = regionsByGroup[group.int("id")].orEmpty().mapNotNull { regionSlugs[it] }.sorted(),
            )
        }.sortedWith(
            compareBy(
                { it.console.ordinal },
                { groups.values.first { group -> group["identifier"] == it.versionGroup }.int("order") },
                { it.slug },
            ),
        )
}

/** Upstream's condition values, restricted to the axes this dataset's encounters actually reference. */
private fun buildEncounterConditions(
    source: UpstreamSource,
    referenced: Set<String>,
): List<EncounterConditionJson> {
    val axes = source.read("encounter_conditions").associate { it.int("id") to it["identifier"] }

    return source.read("encounter_condition_values")
        .filter { it["identifier"] in referenced }
        .map {
            EncounterConditionJson(
                slug = it["identifier"],
                axis = axes.getValue(it.int("encounter_condition_id")),
                isDefault = it.bool("is_default"),
            )
        }.sortedWith(compareBy({ it.axis }, { it.slug }))
}

/**
 * Everything read out of `encounters.csv`, grouped the way the two screens read it.
 *
 * The one transformation applied here is summing a variant's slots **within a single table and a
 * single condition state**. Upstream stores a twelve-slot table as twelve rows and a Pokemon may hold
 * four of them; on screen the four are one 45% line. Summing any wider than that is the mistake the
 * correction on #8 exists to prevent, and it cannot be undone afterwards.
 */
private fun buildEncounters(
    source: UpstreamSource,
    versions: List<VersionJson>,
    knownVariants: Set<String>,
    regionByLocation: Map<String, String>,
): EncounterHarvest {
    val methods = source.read("encounter_methods")
    val methodSlugs = methods.associate { it.int("id") to it["identifier"] }
    val methodOrder = methods.associate { it["identifier"] to it.int("order") }

    val slots =
        source.read("encounter_slots")
            .associate {
                it.int("id") to (methodSlugs.getValue(it.int("encounter_method_id")) to (it.intOrNull("rarity") ?: 0))
            }

    val conditionSlugs = source.read("encounter_condition_values").associate { it.int("id") to it["identifier"] }

    val conditionsByEncounter =
        source.read("encounter_condition_value_map")
            .groupBy({ it.int("encounter_id") }, { conditionSlugs.getValue(it.int("encounter_condition_value_id")) })

    val versionSlugs = source.read("versions").associate { it.int("id") to it["identifier"] }
    val shipped = versions.map { it.slug }.toSet()
    val versionRank = versions.withIndex().associate { (index, version) -> version.slug to index }

    val areas = source.read("location_areas").associateBy { it.int("id") }
    val locationSlugs = source.read("locations").associate { it.int("id") to it["identifier"] }
    val variantSlugs = source.read("pokemon").associate { it.int("id") to it["identifier"] }

    // Grouped all the way down before anything is summed, because the key of the innermost map is
    // exactly the tuple a rate is a percentage of.
    val byRegion =
        mutableMapOf<String, MutableMap<String, MutableMap<String, MutableMap<TableKey, MutableMap<SlotKey, MutableMap<Int, Int>>>>>>()
    val referencedConditions = mutableSetOf<String>()
    var dropped = 0

    source.read("encounters").forEach { row ->
        val version = versionSlugs[row.int("version_id")]?.takeIf { it in shipped }
        val area = areas[row.int("location_area_id")]
        val variant = variantSlugs[row.int("pokemon_id")]?.takeIf { it in knownVariants }
        val slot = slots[row.int("encounter_slot_id")]
        val location = area?.let { locationSlugs[it.int("location_id")] }
        val region = location?.let { regionByLocation[it] }

        if (version == null || variant == null || slot == null || location == null || region == null) {
            dropped++
            return@forEach
        }

        val conditions = conditionsByEncounter[row.int("id")].orEmpty().sorted()
        referencedConditions += conditions

        val key = SlotKey(variant, row.int("min_level"), row.int("max_level"), conditions)
        val table = TableKey(area["identifier"].ifEmpty { location }, slot.first)

        // Keyed by the upstream slot id, and **assigned rather than added**.
        //
        // Rarity is a property of the slot, so a Pokemon holding four of a table's twelve slots is
        // four rarities to add up -- but the *same* slot id arriving twice is one slot written twice,
        // and adding it counts it twice. Upstream does exactly that for Generation II fishing:
        // Cherrygrove's Super Rod table is stored three times over, once per time of day, with the
        // time condition left off all three. Added up it reads as 300%.
        byRegion
            .getOrPut(region) { mutableMapOf() }
            .getOrPut(location) { mutableMapOf() }
            .getOrPut(version) { mutableMapOf() }
            .getOrPut(table) { mutableMapOf() }
            .getOrPut(key) { mutableMapOf() }[row.int("encounter_slot_id")] = slot.second
    }

    val encounters =
        byRegion.map { (region, locations) ->
            RegionEncountersJson(
                region = region,
                locations =
                    locations.map { (location, byVersion) ->
                        LocationEncountersJson(
                            location = location,
                            versions =
                                byVersion.map { (version, tables) ->
                                    VersionEncountersJson(
                                        version = version,
                                        tables =
                                            tables.map { (table, rows) -> table.toJson(rows) }
                                                .sortedWith(compareBy({ methodOrder[it.method] ?: 0 }, { it.area })),
                                    )
                                }.sortedBy { versionRank[it.version] },
                        )
                    }.sortedBy { it.location },
            )
        }.sortedBy { it.region }

    return EncounterHarvest(encounters, referencedConditions, dropped)
}

private data class TableKey(
    val area: String,
    val method: String,
)

private fun TableKey.toJson(rows: Map<SlotKey, Map<Int, Int>>): EncounterTableJson =
    EncounterTableJson(
        area = area,
        method = method,
        slots =
            rows.map { (key, bySlotId) ->
                EncounterSlotJson(
                    variant = key.variant,
                    minLevel = key.minLevel,
                    maxLevel = key.maxLevel,
                    // Summed across the distinct slots this variant holds, never across repeats of one.
                    chance = bySlotId.values.sum(),
                    conditions = key.conditions,
                )
            }.sortedWith(
                compareByDescending<EncounterSlotJson> { it.chance }
                    .thenBy { it.variant }
                    .thenBy { it.conditions.joinToString() },
            ),
    )

private data class SlotKey(
    val variant: String,
    val minLevel: Int,
    val maxLevel: Int,
    val conditions: List<String>,
)

private class EncounterHarvest(
    val regions: List<RegionEncountersJson>,
    val referencedConditions: Set<String>,
    val dropped: Int,
)

/**
 * Every place that belongs to a region this app ships.
 *
 * The 91 locations upstream files under no region are dropped, and so are Orre's eighteen. Nothing
 * can reach either set -- the only route to a location is through the region that lists it -- and the
 * spin-off region is not generated at all. See `SPIN_OFFS`.
 *
 * [LocationJson.versions] is empty here and filled by [withEncounteredVersions] once the encounters
 * are built, because it is a fact about them rather than about the place.
 */
private fun buildLocations(source: UpstreamSource): List<LocationJson> {
    val regionSlugs = source.read("regions").associate { it.int("id") to it["identifier"] }

    val names =
        source.read("location_names")
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("location_id") to it["name"] }

    val areaNames =
        source.read("location_area_prose")
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("location_area_id") to it["name"] }

    val areasByLocation = source.read("location_areas").groupBy { it.int("location_id") }

    val shipped = CURATED_REGIONS.map { it.slug }.toSet()

    return source.read("locations")
        .filter { regionSlugs[it.intOrNull("region_id")] in shipped }
        .map { row ->
            val id = row.int("id")
            val slug = row["identifier"]
            val name = names[id] ?: slug

            LocationJson(
                slug = slug,
                name = name,
                region = regionSlugs.getValue(row.int("region_id")),
                category = locationCategoryOf(slug),
                areas =
                    areasByLocation[id].orEmpty().map { area ->
                        val label = areaNames[area.int("id")]
                        LocationAreaJson(
                            slug = area["identifier"].ifEmpty { slug },
                            // Upstream names a location's main area after the location itself, so a
                            // screen drawing it beside the heading would print the same words twice.
                            name = label?.takeIf { it != name && area["identifier"].isNotEmpty() },
                        )
                    }.sortedBy { it.slug },
                versions = emptyList(),
            )
        }.sortedBy { it.slug }
}

private fun List<LocationJson>.withEncounteredVersions(encounters: List<RegionEncountersJson>): List<LocationJson> {
    val versionsByLocation =
        encounters
            .flatMap { it.locations }
            .associate { entry -> entry.location to entry.versions.map { it.version } }

    return map { it.copy(versions = versionsByLocation[it.slug].orEmpty()) }
}

/** The ten main-series regions, their curated facts, and the regional dex each one is the dex of. */
private fun buildRegions(
    source: UpstreamSource,
    versions: List<VersionJson>,
    locations: List<LocationJson>,
): List<RegionJson> {
    val regionNames = source.read("region_names")

    val names =
        regionNames
            .filter { it.int("local_language_id") == ENGLISH }
            .associate { it.int("region_id") to it["name"] }

    // Upstream's `ja-hrkt`, which is the name every other Pokedex shows. Present for all ten, now
    // that the one region it was missing for is not generated.
    val nativeNames =
        regionNames
            .filter { it.int("local_language_id") == JAPANESE }
            .associate { it.int("region_id") to it["name"] }

    val pokedexIds =
        source.read("pokedexes")
            .filter { it.bool("is_main_series") }
            .associate { it["identifier"] to it.int("id") }

    val dexEntries = source.read("pokemon_dex_numbers").groupBy { it.int("pokedex_id") }
    val speciesSlugs = source.read("pokemon_species").associate { it.int("id") to it["identifier"] }
    val variantsBySpecies = source.read("pokemon").groupBy({ it.int("species_id") }, { it["identifier"] })

    val locationCounts = locations.groupingBy { it.region }.eachCount()
    val curated = CURATED_REGIONS.associateBy { it.slug }

    // Ordered by upstream's own id, which runs Kanto to Paldea. Filtered to the curated table, which
    // is what leaves Orre out: it is a spin-off region whose only games are Colosseum and XD, and
    // this app covers the main series. See `SPIN_OFFS`.
    return source.read("regions")
        .filter { it["identifier"] in curated }
        .sortedBy { it.int("id") }
        .map { row ->
            val id = row.int("id")
            val slug = row["identifier"]
            val entry = curated.getValue(slug)

            // Upstream's own region-to-version-group table, and nothing else.
            //
            // This used to fall back to the versions that have encounters here, because
            // `version_group_regions` has no row for Orre and that left the region with no games at
            // all. Orre is no longer generated, so the fallback has nothing left to rescue -- and it
            // was never safe: two rows put Black and White inside `team-flare-secret-hq`, a Kalos
            // location, which read Kalos as a Generation V region.
            val regionVersions = versions.filter { slug in it.regions }

            RegionJson(
                slug = slug,
                name = names[id] ?: slug,
                nativeName = nativeNames.getValue(id),
                // The generation a region belongs to is the earliest its own games are from. Kanto is
                // Generation I even though Gold and Silver reach it: those are Johto's games.
                generation = regionVersions.minOfOrNull { it.generation } ?: 0,
                blurb = entry.blurb,
                boxArt = entry.boxArt,
                pokedex =
                    entry.pokedexes
                        // Sorted inside each dex and then concatenated, rather than sorted across all
                        // of them. Kalos ships three dexes that each number from 1, so a sort on the
                        // number alone would interleave them into three overlapping runs of 1..153.
                        .flatMap { dex ->
                            dexEntries[pokedexIds.getValue(dex)].orEmpty().sortedBy { it.int("pokedex_number") }
                        }.mapNotNull { dexRow ->
                            val speciesId = dexRow.int("species_id")
                            val species = speciesSlugs[speciesId] ?: return@mapNotNull null
                            RegionDexEntryJson(
                                number = dexRow.int("pokedex_number"),
                                variant = regionNativeVariant(species, slug, variantsBySpecies[speciesId].orEmpty()),
                            )
                        }.distinctBy { it.variant },
                versions = regionVersions.map { it.slug },
                locationCount = locationCounts[slug] ?: 0,
            )
        }
}

/**
 * Which form of a species a regional dex means.
 *
 * #5's correction: a regional dex shows the variant native to that region, so Kanto's #037 is the
 * Kantonian Vulpix and Alola's is the Alolan one. Regional forms are suffixed with their region --
 * the same naming the variant key already leans on -- so the slug is the whole match and nothing
 * extra needs storing.
 *
 * Every region with no regional forms of its own, which is Kanto through Unova, falls through to the
 * default variant.
 */
private fun regionNativeVariant(
    species: String,
    region: String,
    variants: List<String>,
): String = variants.firstOrNull { it == "$species-$region" } ?: species
