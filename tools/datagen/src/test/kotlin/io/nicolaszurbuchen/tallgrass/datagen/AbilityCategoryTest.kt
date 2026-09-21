package io.nicolaszurbuchen.tallgrass.datagen

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Guards the **order** of the rules, which is the half of the taxonomy that is not in the enum.
 *
 * Every case here is one where two rules both match and the question is which wins. The cases where
 * only one rule matches are not worth a test: they are the regex reading its own keyword back.
 *
 * Accuracy on the whole set is not tested and is not the point. This is a keyword pass over prose
 * upstream wrote for another purpose, and what makes that acceptable is
 * `data/ability-categories.json` rather than the rules getting every one of the 314 right.
 */
class AbilityCategoryTest {
    @Test
    fun anAbilityThatSummonsWeatherAndBoostsAStat_readsAsTheWeather() {
        // Swift Swim. Someone filtering for rain abilities looks under the rain, not under Speed.
        assertEquals(AbilityCategory.ENVIRONMENT, classifyAbility("Doubles Speed during rain."))
    }

    @Test
    fun aTradeOffAbility_readsByItsUpside() {
        // Hustle. Neither direction rule can tell whose stat moved -- upstream writes "decreases
        // their accuracy" for the bearer's own and "lowers opponents' Attack" for someone else's --
        // so OFFENSE running first is what stops a downside deciding the category.
        assertEquals(
            AbilityCategory.OFFENSE,
            classifyAbility("Strengthens physical moves to inflict 1.5× damage, but decreases their accuracy to 0.8×."),
        )
    }

    @Test
    fun theDirectionCut_separatesTwoAbilitiesThatShareAMechanism() {
        // The reason there is no STATS member. Both move a stat stage; they are opposite intents.
        assertEquals(AbilityCategory.OFFENSE, classifyAbility("Raises Attack one stage upon KOing a Pokémon."))
        assertEquals(AbilityCategory.DEFENSE, classifyAbility("Lowers opponents' Attack one stage upon entering battle."))
    }

    @Test
    fun loweringADefensiveStat_isOffence() {
        // Sword of Ruin. The mirror of the rule above, and the case that shows the cut is about
        // direction rather than about which stat is named.
        assertEquals(AbilityCategory.OFFENSE, classifyAbility("Lowers Defense of all Pokémon except itself."))
    }

    @Test
    fun anAbilityThatHealsBecauseItIsImmune_readsAsTheImmunity() {
        // Volt Absorb. What it is for is being immune to Electric; the heal is the consequence.
        assertEquals(AbilityCategory.IMMUNITY, classifyAbility("Absorbs Electric moves, healing for 1/4 max HP."))
    }

    @Test
    fun changingTypeWhenHit_isAFormChangeRatherThanAReaction() {
        // Color Change. FORM sits above REACTIVE for this one case.
        assertEquals(AbilityCategory.FORM, classifyAbility("Changes type to match when hit by a damaging move."))
    }

    @Test
    fun convertingMoveTypes_isNotAFormChange() {
        // Refrigerate. FORM is about the bearer's own form or type; this is something it does to its
        // moves, and the power boost is what a reader would filter on.
        assertEquals(
            AbilityCategory.OFFENSE,
            classifyAbility("Turns the bearer's Normal moves into Ice moves and strengthens them to 1.3× their power."),
        )
    }

    @Test
    fun firingOnFainting_isReactiveEvenThoughNothingTouchedIt() {
        // Why the member is REACTIVE and not CONTACT: contact is the largest case, not the whole.
        assertEquals(
            AbilityCategory.REACTIVE,
            classifyAbility("When this Pokémon faints from an opponent's move, that opponent takes damage."),
        )
    }

    @Test
    fun anythingUnmatched_fallsToUtility() {
        // The partition has to be total, and UTILITY is where a new mechanic lands until somebody
        // looks at it. It is named honestly rather than as "Other".
        assertEquals(AbilityCategory.UTILITY, classifyAbility("Picks up other Pokémon's used and Flung held items."))
        assertEquals(AbilityCategory.UTILITY, classifyAbility(""))
    }
}
