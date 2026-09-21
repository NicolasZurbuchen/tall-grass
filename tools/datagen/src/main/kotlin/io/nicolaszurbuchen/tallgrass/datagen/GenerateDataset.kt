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
    val abilities = buildAbilities(source, readTagOverrides(outputDir))
    val moves = buildMoves(source)

    outputDir.resolve("types.json").writeText(json.encodeToString(types))
    outputDir.resolve("species.json").writeText(json.encodeToString(species))
    outputDir.resolve("variants.json").writeText(json.encodeToString(variants))
    outputDir.resolve("abilities.json").writeText(json.encodeToString(abilities))
    outputDir.resolve("moves.json").writeText(json.encodeToString(moves))

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
        )
    outputDir.resolve("manifest.json").writeText(json.encodeToString(manifest))

    println("  species            ${manifest.speciesCount}")
    println("  variants           ${manifest.variantCount}")
    println("  listed in the dex  ${manifest.listedVariantCount}")
    println("  types              ${manifest.typeCount}")
    println("  abilities          ${manifest.abilityCount}")
    println("  moves              ${manifest.moveCount}")

    abilities
        .flatMap { it.tags }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .forEach { (tag, count) -> println("    ${tag.name.padEnd(14)} $count") }

    println("  untagged           ${abilities.count { it.tags.isEmpty() }}")
    println("  tags per ability   ${abilities.groupingBy { it.tags.size }.eachCount().toSortedMap()}")
    println("  triggers           ${abilities.groupingBy { it.trigger }.eachCount().entries.sortedByDescending { it.value }}")
}

/**
 * The hand-authored corrections, which this program reads and never writes.
 *
 * #27 requires that a fixed category stay fixed, and a generator that rewrites `abilities.json`
 * wholesale would eat the fix on the next SHA bump. Keeping the human's input in its own file means
 * the classifier's output stays pure and fully regenerated -- so a rerun with no upstream change
 * produces an empty diff -- while a correction reads as its own line rather than as a hunk inside
 * generated output.
 *
 * DECISIONS.md, An ability's category is classified, overridden by hand, and committed
 */
private fun readTagOverrides(datasetDir: File): Map<String, List<AbilityTag>> {
    val file = datasetDir.resolve("ability-tags.json")
    if (!file.exists()) return emptyMap()

    return json.decodeFromString<Map<String, List<AbilityTag>>>(file.readText())
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
private fun buildAbilities(
    source: UpstreamSource,
    overrides: Map<String, List<AbilityTag>>,
): List<AbilityJson> {
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
                // The override replaces the list outright rather than adding to it, because the two
                // ways the classifier goes wrong are opposite -- a loose pattern over-tags and a
                // narrow one misses -- and a merge could only fix the second.
                // Sorted and de-duplicated here rather than at either source, so a hand-written
                // override does not have to know the enum's declaration order to produce a stable
                // diff.
                tags = (overrides[slug] ?: classifyAbility(shortEffect)).distinct().sortedBy { it.ordinal },
                trigger = triggerOf(shortEffect),
                shortEffect = shortEffect,
                // Paragraphs, and upstream writes them with a blank line between. Collapsed to one
                // newline so the screen decides the spacing rather than inheriting a wiki's.
                effect = entry["effect"].replace(BLANK_LINE, "\n").trim(),
            )
        }.sortedBy { it.slug }
}

/** Upstream separates paragraphs with a blank line, sometimes several. */
private val BLANK_LINE = Regex("""\n\s*\n+""")

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

    return source.read("moves")
        .filter { it.int("id") < FIRST_SPIN_OFF_ID }
        .map { row ->
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
                effectChance = row.intOrNull("effect_chance"),
                shortEffect = entry?.get("short_effect"),
                effect = entry?.get("effect")?.replace(BLANK_LINE, "\n")?.trim(),
            )
        }.sortedBy { it.slug }
}
