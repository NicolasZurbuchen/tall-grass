package io.nicolaszurbuchen.tallgrass.datagen

import kotlinx.serialization.Serializable

/**
 * The scanning axis for a list of 314 abilities. Upstream has no category field at all, so this is
 * original work rather than a field being copied. See #27.
 *
 * Derived by reading every English `short_effect` rather than from either of the sets on record: the
 * design file's `Common / Weather / Recovery / Defensive` and the prototype's
 * `Pinch / Weather / Contact / Utility / Offensive` were both written before the data existed, and
 * neither has a home for the eighteen form changers or the sixty-nine immunities.
 *
 * [OFFENSE] and [DEFENSE] are cut by **direction, not by mechanism**. An earlier set had a `STATS`
 * member holding everything that moved a stat stage, which put Moxie beside Intimidate — one raises
 * the bearer's own Attack and the other lowers the opponent's, which are opposite intents sharing a
 * mechanism. Nobody browsing filters on the mechanism.
 *
 * [UTILITY] is the residue and is named honestly: held items, turn order, information, out-of-battle
 * effects, and abilities that act on other abilities. Roughly one in six.
 */
@Serializable
enum class AbilityCategory {
    ENVIRONMENT,
    FORM,
    REACTIVE,
    IMMUNITY,
    RECOVERY,
    DEFENSE,
    OFFENSE,
    UTILITY,
}

/**
 * Classifies one ability from its `short_effect`. First match wins, so the order of the rules below
 * is half the taxonomy: an ability that summons rain *and* doubles Speed is [AbilityCategory.ENVIRONMENT],
 * because the weather is what it is known for and what someone filtering would look under.
 *
 * **This is expected to be wrong about some of them.** It is a keyword pass over prose upstream
 * wrote for a different purpose, and the mechanism that makes that acceptable is not accuracy here
 * but `data/ability-categories.json`, which a human owns and which wins. See #27 and
 * `DECISIONS.md § An ability's category is classified, overridden by hand, and committed`.
 */
fun classifyAbility(shortEffect: String): AbilityCategory =
    RULES.firstOrNull { (_, pattern) -> pattern.containsMatchIn(shortEffect) }?.first ?: AbilityCategory.UTILITY

/**
 * Stat names, as a fragment reused by the two direction rules below. Written out rather than matched
 * as "stat" because upstream's prose names the stat far more often than it uses the word.
 */
private const val OFFENSIVE_STAT = "Attack|Special Attack|Speed|accuracy|critical"
private const val DEFENSIVE_STAT = "Defense|Special Defense|evasion"

/**
 * Weather and terrain together, which is why the member is not called `WEATHER`: a third of what
 * lands here is terrain — the four Surges, Grass Pelt, Quark Drive, Mimicry.
 */
private val ENVIRONMENT =
    Regex("""\b(weather|rain|sunlight|sandstorm|hail|snows?|snowing|terrain|Booster Energy)\b""", RegexOption.IGNORE_CASE)

/**
 * The bearer's own form or type, and nothing else.
 *
 * Above [REACTIVE], because several of these change type *when hit* and the change is the point:
 * Color Change would otherwise read as a reaction.
 *
 * A move-type conversion is deliberately not here. Refrigerate turns the bearer's Normal moves into
 * Ice moves and strengthens them, which is something it does to its moves rather than to itself, so
 * it reads as [OFFENSE]; Liquid Voice, which converts without strengthening, falls to
 * [AbilityCategory.UTILITY].
 */
private val FORM =
    Regex(
        """\bform(e|es|s)?\b|transforms?|changes? [^.]*\btype\b|\btype\b[^.]* to match|takes the appearance""",
        RegexOption.IGNORE_CASE,
    )

/**
 * Reactive rather than contact: Aftermath and Innards Out fire on fainting, and Stamina on taking
 * damage from any move at all. Contact is the largest case, not the whole of it.
 */
private val REACTIVE =
    Regex(
        """\bcontact\b|attacking Pok|upon being hit|when hit by|hit by (a|an) |""" +
            """when (it|the Pokémon|this Pokémon) (is |takes |takes regular )?(hit|damage)|""" +
            """takes damage from (a |an )?(move|physical|contact)|when (this Pokémon |it )?faints""",
        RegexOption.IGNORE_CASE,
    )

/** Above [RECOVERY]: Volt Absorb heals, but what it is *for* is being immune to Electric. */
private val IMMUNITY =
    Regex(
        """\b(prevents?|protects?|immune|immunity|absorbs?|evades|bypass(es)?|ignores?|negates?|nullifies)\b|""" +
            """cannot (be|lower|use|eat)""",
        RegexOption.IGNORE_CASE,
    )

private val RECOVERY = Regex("""\b(heals?|healing|restores?|cures?|curing|regains?)\b""", RegexOption.IGNORE_CASE)

/**
 * Deals more damage: a multiplier on outgoing damage, the bearer's own offensive stats going up, or
 * the opponent's defensive stats coming down.
 *
 * **Above [DEFENSE], and that order is load-bearing.** Neither rule can tell *whose* stat moved —
 * upstream writes "decreases their accuracy" for the bearer's own and "lowers opponents' Attack" for
 * someone else's, and no keyword separates them. Running this one first makes a trade-off ability
 * read by its upside, which is what it is named for: Hustle strengthens physical moves at the cost
 * of its own accuracy, and filing it under [DEFENSE] for that accuracy drop was the first thing this
 * classifier got wrong.
 */
private val OFFENSE =
    Regex(
        """\b(strengthens?|critical)\b|powers? up|double(s)? (the )?(power|damage)|[0-9.]+× (their |its )?power|""" +
            """increases? [^.]{0,30}damage|inflicts? [^.]* damage|damage inflicted|boosts? the power|""" +
            """power (is |are )?(increased|boosted|doubled)|have [0-9.]+× power|same-type attack bonus|""" +
            """\b(raises?|rises?|increases?|boosts?|doubles?)\b[^.]{0,40}\b($OFFENSIVE_STAT)\b|""" +
            """\b($OFFENSIVE_STAT)\b[^.]{0,40}\b(rises?|(is|are) (raised|boosted|increased))\b|""" +
            """\b(lowers?|decreases?)\b[^.]{0,40}\b($DEFENSIVE_STAT)\b""",
        RegexOption.IGNORE_CASE,
    )

/**
 * Takes less damage, by the mirror of the test above. Intimidate lands here, which is the whole
 * reason the two are cut by direction rather than by mechanism.
 */
private val DEFENSE =
    Regex(
        """halves? damage|reduces? [^.]*damage|decreases? [^.]* damage|damage [^.]*(is )?halved|""" +
            """damage taken|takes? less|not be very effective|""" +
            """\b(raises?|rises?|increases?|boosts?|doubles?)\b[^.]{0,40}\b($DEFENSIVE_STAT)\b|""" +
            """\b($DEFENSIVE_STAT)\b[^.]{0,40}\b(rises?|(is|are) (raised|boosted|increased))\b|""" +
            """\b(lowers?|decreases?)\b[^.]{0,40}\b($OFFENSIVE_STAT)\b""",
        RegexOption.IGNORE_CASE,
    )

/**
 * The rules, in the order they are tried. One named value each rather than a list of literals, so
 * each can carry the reason it sits where it does.
 *
 * Declared last because Kotlin initialises top-level properties in source order: a list that names
 * them from above sees every one as null.
 */
private val RULES: List<Pair<AbilityCategory, Regex>> =
    listOf(
        AbilityCategory.ENVIRONMENT to ENVIRONMENT,
        AbilityCategory.FORM to FORM,
        AbilityCategory.REACTIVE to REACTIVE,
        AbilityCategory.IMMUNITY to IMMUNITY,
        AbilityCategory.RECOVERY to RECOVERY,
        AbilityCategory.OFFENSE to OFFENSE,
        AbilityCategory.DEFENSE to DEFENSE,
    )
