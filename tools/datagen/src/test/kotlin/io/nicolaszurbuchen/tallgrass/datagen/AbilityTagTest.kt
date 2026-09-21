package io.nicolaszurbuchen.tallgrass.datagen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the boundaries between tags, which is where the judgement lives.
 *
 * Most of these are abilities that do **two** things. Under the single category this replaced, each
 * one lost a fact to a precedence order; the tests exist to keep both facts, not to check that a
 * regex reads its own keyword back.
 *
 * Accuracy over the whole set is not tested and is not the point. This is a keyword pass over prose
 * upstream wrote for another purpose, and what makes that acceptable is `data/ability-tags.json`.
 */
class AbilityTagTest {
    @Test
    fun anAbilityThatDoesTwoThings_keepsBothFacts() {
        // Chlorophyll. The case that killed the single-category model: filed under the weather, its
        // Speed was invisible; filed under Speed, its weather was.
        assertEquals(
            listOf(AbilityTag.STATS_OFFENSE, AbilityTag.WEATHER),
            classifyAbility("Doubles Speed during strong sunlight."),
        )
    }

    @Test
    fun immunityIsAllOrNothing_andPartialReductionIsNot() {
        // The boundary that earns IMMUNITY its place: Levitate takes zero, Thick Fat takes half, and
        // a player looking for one is not looking for the other.
        assertEquals(listOf(AbilityTag.IMMUNITY), classifyAbility("Evades Ground moves."))
        assertEquals(listOf(AbilityTag.DAMAGE_TAKEN), classifyAbility("Halves damage from Fire and Ice moves."))
    }

    @Test
    fun immunityCombinesWithWhateverItIsAnImmunityTo() {
        // Which is why it can be about completeness rather than about the subject: Limber says both
        // "nothing gets through" and "the thing is a status".
        assertEquals(listOf(AbilityTag.IMMUNITY, AbilityTag.STATUS), classifyAbility("Prevents paralysis."))
        assertEquals(
            listOf(AbilityTag.IMMUNITY, AbilityTag.STATS),
            classifyAbility("Prevents stats from being lowered by other Pokémon."),
        )
    }

    @Test
    fun theStatTagsAreCutByWhoTheChangeHelps_notByWhichWayTheNumberMoved() {
        // Lowering an opponent's Attack is defensive even though the number went down, and that is
        // the whole reason there are two of them rather than an "up" and a "down".
        assertEquals(listOf(AbilityTag.STATS_OFFENSE), classifyAbility("Raises Attack one stage upon KOing a Pokémon."))
        assertEquals(
            listOf(AbilityTag.STATS_DEFENSE),
            classifyAbility("Lowers opponents' Attack one stage upon entering battle."),
        )
    }

    @Test
    fun aStatChangeWrittenBackToFront_isStillAStatChange() {
        // Soul-Heart and Supreme Overlord. Upstream writes both "Raises Attack" and "its Attack is
        // raised", and a verb-first rule alone silently loses the second.
        assertEquals(
            listOf(AbilityTag.STATS_OFFENSE),
            classifyAbility("This Pokémon's Special Attack rises by one stage every time any Pokémon faints."),
        )
    }

    @Test
    fun aStatMultiplier_isAStatAndNotADamageMultiplier() {
        // Huge Power against Blaze. Both mean "I hit harder", and the difference is whether Haze,
        // Clear Body and Unaware can touch it -- which is exactly what a player is choosing between.
        assertEquals(listOf(AbilityTag.STATS_OFFENSE), classifyAbility("Doubles Attack in battle."))
        assertEquals(
            listOf(AbilityTag.DAMAGE_DEALT),
            classifyAbility("Strengthens Fire moves to inflict 1.5× damage at 1/3 max HP or less."),
        )
    }

    @Test
    fun statusStemsMatchTheWordsUpstreamActuallyWrites() {
        // The regression this pins cost a single character. A trailing \b after "poison" matches the
        // noun and not "poisoning", and upstream writes the participle for every contact ability in
        // the game -- Static, Flame Body, Poison Point, Cute Charm all silently lost their tag.
        assertTrue(AbilityTag.STATUS in classifyAbility("Has a 30% chance of poisoning attacking Pokémon on contact."))
        assertTrue(AbilityTag.STATUS in classifyAbility("Has a 30% chance of burning attacking Pokémon on contact."))
        assertTrue(AbilityTag.STATUS in classifyAbility("Has a 30% chance of paralyzing attacking Pokémon on contact."))
    }

    @Test
    fun anAbilityWithNoPlayerIntent_carriesNoTags() {
        // Heavy Metal. The partition is not forced to be total: a card with no chip is honest, and
        // an OTHER tag would be a bucket nobody would ever filter by.
        assertEquals(emptyList(), classifyAbility("Doubles the Pokémon's weight."))
    }

    @Test
    fun theTriggerIsSeparateFromWhatTheAbilityDoes() {
        // Electromorphosis, which is what showed the first list was two taxonomies in one: "contact"
        // is when it fires and "deals more damage" is what it does, and neither implies the other.
        val effect = "When hit by an attack, the power of the next Electric-type move it uses is doubled."

        assertEquals(listOf(AbilityTag.DAMAGE_DEALT), classifyAbility(effect))
        assertEquals(AbilityTrigger.ON_HIT, triggerOf(effect))
    }

    @Test
    fun faintingAndMakingSomethingFaint_areOppositeTriggers() {
        // Written with the same verb, which is why ON_FAINT is asked first.
        assertEquals(
            AbilityTrigger.ON_FAINT,
            triggerOf("When this Pokémon faints from an opponent's move, that opponent takes damage."),
        )
        assertEquals(AbilityTrigger.ON_KO, triggerOf("Raises Attack one stage upon KOing a Pokémon."))
    }

    @Test
    fun anAbilityThatIsSimplyAlwaysOn_isPassive() {
        assertEquals(AbilityTrigger.PASSIVE, triggerOf("Doubles Speed during strong sunlight."))
        assertEquals(AbilityTrigger.PASSIVE, triggerOf(""))
    }
}
