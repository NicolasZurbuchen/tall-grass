package io.nicolaszurbuchen.tallgrass.datagen

import kotlinx.serialization.Serializable

/**
 * What an ability does, as the questions a player is actually asking when they browse a list of 314
 * of them. Upstream has no such field, so this is original work rather than one being copied. See #27.
 *
 * **An ability carries as many of these as are true**, which is the property the whole design turns
 * on. An earlier version of this was a single category decided by a precedence order, and every
 * multi-faceted ability lost a fact it should have kept: Chlorophyll is weather *and* speed, Aura
 * Guard is contact *and* mitigation, Anger Shell moves stats in both directions at once. A precedence
 * order made those choices consistently, which is not the same as making them correctly.
 *
 * **When an ability fires is not in here.** That is [AbilityTrigger], because "contact" is a trigger
 * and "deals more damage" is an effect, and a list holding both is two taxonomies wearing one name.
 *
 * [IMMUNITY] is all-or-nothing and is what separates it from [DAMAGE_TAKEN]: Levitate evades Ground
 * moves entirely and Thick Fat halves them. It combines with whatever the immunity is *to* — Limber
 * is [IMMUNITY] and [STATUS], Clear Body is [IMMUNITY] and [STATS] — so nothing is lost by it being
 * about the completeness rather than about the subject.
 *
 * The three stat tags are a family. [STATS_OFFENSE] and [STATS_DEFENSE] are cut by which side the
 * change helps, not by which direction the number moved: lowering an opponent's Attack is defensive.
 * [STATS] is for the ones with no direction at all — Contrary inverts, Simple doubles, Moody is
 * random.
 */
@Serializable
enum class AbilityTag {
    DAMAGE_DEALT,
    DAMAGE_TAKEN,
    IMMUNITY,
    STATS,
    STATS_OFFENSE,
    STATS_DEFENSE,
    STATUS,
    HEALING,
    WEATHER,
    FORM,
    ITEMS,
    ABILITIES,
    PRIORITY,
    SWITCHING,
    MOVES,
    OVERWORLD,
}

/**
 * When an ability fires.
 *
 * Single-valued, unlike [AbilityTag], and the difference is a fact about the two questions rather
 * than a simplification: "what does it do" genuinely has several answers and "when does it fire"
 * mostly has one. [PASSIVE] is the fallback and the largest member — an ability that is simply always
 * on, however many conditions its effect is written with.
 */
@Serializable
enum class AbilityTrigger {
    PASSIVE,
    ON_ENTRY,
    ON_HIT,
    ON_KO,
    ON_FAINT,
    ON_SWITCH_OUT,
    END_OF_TURN,
    LOW_HP,
}

/**
 * Every tag whose pattern matches, in declaration order.
 *
 * **There is no precedence here and that is the point.** Each rule is asked independently, so two
 * facts about one ability produce two tags rather than a contest one of them loses.
 *
 * The cost is the opposite failure: a loose pattern over-tags rather than mis-tags. Both are fixed
 * the same way, by `data/ability-tags.json`, which replaces an ability's list outright.
 */
fun classifyAbility(shortEffect: String): List<AbilityTag> =
    TAG_RULES.filter { (_, pattern) -> pattern.containsMatchIn(shortEffect) }.map { it.first }

/**
 * The first trigger that matches, falling back to [AbilityTrigger.PASSIVE].
 *
 * Ordered, because unlike the tags these really are alternatives. The order that matters is
 * [AbilityTrigger.ON_FAINT] above [AbilityTrigger.ON_KO]: "when this Pokemon faints" and "when it
 * faints another Pokemon" are opposite events written with the same verb.
 */
fun triggerOf(shortEffect: String): AbilityTrigger =
    TRIGGER_RULES.firstOrNull { (_, pattern) -> pattern.containsMatchIn(shortEffect) }?.first ?: AbilityTrigger.PASSIVE

/** Reused by the two directional stat rules. Written out because the prose names the stat, not "stat". */
private const val OFFENSIVE_STAT = "Attack|Special Attack|Speed|accuracy|critical hit rate"
private const val DEFENSIVE_STAT = "Defense|Special Defense|evasion"

private const val RAISE = "raises?|rises?|increases?|boosts?|doubles?"
private const val LOWER = "lowers?|decreases?|halves?"

private fun ignoreCase(pattern: String) = Regex(pattern, RegexOption.IGNORE_CASE)

/**
 * Complete negation, which is the whole of what this tag claims. Anything partial is
 * [AbilityTag.DAMAGE_TAKEN] instead.
 */
private val IMMUNITY =
    ignoreCase(
        """\b(prevents?|protects?|immune|immunity|evades|absorbs?|negates?|nullifies)\b|""" +
            """cannot (be|use|eat|lower)|\bdo(es)? not (make|take)\b""",
    )

/** Partial reduction: a multiplier or a threshold on damage received, never a stat. */
private val DAMAGE_TAKEN =
    ignoreCase(
        """halves? damage|takes? half damage|reduces? [^.]*damage|decreases? [^.]* damage|""" +
            """damage [^.]*(is )?halved|damage taken|not be very effective|takes? less""",
    )

/** A multiplier on damage dealt, or damage inflicted outside a move. Never a stat. */
private val DAMAGE_DEALT =
    ignoreCase(
        """\bstrengthens?\b|powers? up|double(s)? (the )?(power|damage)|[0-9.]+× (their |its )?power|""" +
            """increases? [^.]{0,30}damage|damage inflicted|boosts? the power|""" +
            """\bpower\b[^.]{0,40}(is |are )?(increased|boosted|doubled)|have [0-9.]+× power|""" +
            """same-type attack bonus|hit twice|full length|""" +
            """\bdamages?\b [^.]*(Pok|opponent|attacker|target)|critical hit""",
    )

// Both directions are matched verb-first *and* stat-first, because upstream writes it both ways:
// "Raises Attack one stage" and "its Attack and Special Attack are boosted" are the same fact.
private const val RAISED = "rises?|(is|are) (raised|boosted|increased|doubled)"
private const val LOWERED = "falls?|(is|are) (lowered|decreased|halved)"

private val STATS_OFFENSE =
    ignoreCase(
        """\b($RAISE)\b[^.]{0,40}\b($OFFENSIVE_STAT)\b|\b($OFFENSIVE_STAT)\b[^.]{0,40}\b($RAISED)\b|""" +
            """\b($LOWER)\b[^.]{0,40}\b($DEFENSIVE_STAT)\b|\b($DEFENSIVE_STAT)\b[^.]{0,40}\b($LOWERED)\b""",
    )

private val STATS_DEFENSE =
    ignoreCase(
        """\b($RAISE)\b[^.]{0,40}\b($DEFENSIVE_STAT)\b|\b($DEFENSIVE_STAT)\b[^.]{0,40}\b($RAISED)\b|""" +
            """\b($LOWER)\b[^.]{0,40}\b($OFFENSIVE_STAT)\b|\b($OFFENSIVE_STAT)\b[^.]{0,40}\b($LOWERED)\b""",
    )

/** Stat mechanics with no direction — inverting, doubling, resetting, copying, protecting. */
private val STATS =
    ignoreCase(
        """stat (modifiers|changes|boosts)|\binverts\b|stats from being lowered|random stat|""" +
            """highest stat|resets all stat|stat-lowering|stat changes|\bstats\b""",
    )

// Stems, so no trailing word boundary: upstream writes "paralyzing", "poisoning", "burning" and
// "infatuating" far more often than the noun, and a \b after the stem matches none of them. That one
// character was silently losing every contact status-inflictor in the game.
private val STATUS =
    ignoreCase(
        """\b(paralys|paralyz|poison|burn|freez|frozen|sleep|asleep|drowsi|confus|infatuat|flinch)|""" +
            """status (ailment|condition|move)|major status""",
    )

/** HP only. Curing a condition is [AbilityTag.STATUS], which several of these also carry. */
private val HEALING = ignoreCase("""\b(heals?|healing|regains?)\b|restores? HP|restoring""")

private val WEATHER =
    ignoreCase("""\b(weather|rain|sunlight|sandstorm|hail|snows?|snowing|terrain|Booster Energy)\b""")

/** The bearer's own form or type. A move-type conversion is not a form change. */
private val FORM =
    ignoreCase("""\bform(e|es|s)?\b|transforms?|changes? [^.]*\btype\b|\btype\b[^.]* to match|takes the appearance""")

private val ITEMS = ignoreCase("""\b(items?|Berry|Berries|Plate|Memory)\b|Pok(é|e)́? ?Ball""")

private val ABILITIES = ignoreCase("""\babilit(y|ies)\b""")

private val PRIORITY = ignoreCase("""\bpriority\b|moves? (first|last)|acts? last|move last""")

private val SWITCHING = ignoreCase("""\b(fle(e|eing)|switch(es|ing)? out|switching|forced out|forcing)\b""")

/**
 * How the bearer's moves behave — what they are typed as, what they can hit, how often their extra
 * effects fire, what they cost.
 *
 * Added after the first run, when the twenty abilities that ended up with no tag at all turned out to
 * be one cluster rather than a residue: No Guard, Scrappy, Serene Grace, Normalize, Piercing Drill,
 * Infiltrator, Pressure. "Which abilities change how my moves work" is a question a player asks, and
 * nothing else on this list answered it.
 */
private val MOVES =
    ignoreCase(
        """\bmoves'|moves? (become|are|act)\b|PP cost|extra effects|Ensures all moves|""" +
            """hit Ghost|through Protect|protecting themselves|Light Screen|Safeguard|""" +
            """non-damaging moves|two-to-five-hit|full length|dance move|act Normal-type""",
    )

private val OVERWORLD = ignoreCase("""wild (encounter|battles)|after battle|picks? up""")

/**
 * Every rule, asked independently. Declared after the patterns because Kotlin initialises top-level
 * properties in source order.
 */
private val TAG_RULES: List<Pair<AbilityTag, Regex>> =
    listOf(
        AbilityTag.DAMAGE_DEALT to DAMAGE_DEALT,
        AbilityTag.DAMAGE_TAKEN to DAMAGE_TAKEN,
        AbilityTag.IMMUNITY to IMMUNITY,
        AbilityTag.STATS to STATS,
        AbilityTag.STATS_OFFENSE to STATS_OFFENSE,
        AbilityTag.STATS_DEFENSE to STATS_DEFENSE,
        AbilityTag.STATUS to STATUS,
        AbilityTag.HEALING to HEALING,
        AbilityTag.WEATHER to WEATHER,
        AbilityTag.FORM to FORM,
        AbilityTag.ITEMS to ITEMS,
        AbilityTag.ABILITIES to ABILITIES,
        AbilityTag.PRIORITY to PRIORITY,
        AbilityTag.SWITCHING to SWITCHING,
        AbilityTag.MOVES to MOVES,
        AbilityTag.OVERWORLD to OVERWORLD,
    )

private val TRIGGER_RULES: List<Pair<AbilityTrigger, Regex>> =
    listOf(
        // Above ON_KO: "when this Pokemon faints" and "when it faints another" are opposite events
        // written with the same verb.
        AbilityTrigger.ON_FAINT to
            ignoreCase("""when (this|the) Pok[^.]{0,20}faints|when knocked out|upon fainting|is knocked out"""),
        AbilityTrigger.ON_KO to
            ignoreCase("""upon KOing|faints? another|after knocking out|when it faints a|any Pok[^.]{0,20}faints|ally faints"""),
        AbilityTrigger.ON_ENTRY to
            ignoreCase("""upon entering (the )?battle|enters (the |a )?battle|upon being sent out|entering battlefield"""),
        AbilityTrigger.ON_SWITCH_OUT to ignoreCase("""upon switching out|when switching out|switches out"""),
        AbilityTrigger.ON_HIT to
            ignoreCase(
                """on contact|upon being hit|when hit by|hit by (a|an) |attacking Pok|""" +
                    """when (it|the Pok[^.]{0,10}|this Pok[^.]{0,10}) (is |takes |takes regular )?(hit|damage)|""" +
                    """takes damage from""",
            ),
        AbilityTrigger.LOW_HP to
            ignoreCase("""HP [^.]{0,30}(below|or less)|drops below (half|50)|max HP or less|HP is 25%"""),
        AbilityTrigger.END_OF_TURN to ignoreCase("""after each turn|each turn|end of the next turn"""),
    )
