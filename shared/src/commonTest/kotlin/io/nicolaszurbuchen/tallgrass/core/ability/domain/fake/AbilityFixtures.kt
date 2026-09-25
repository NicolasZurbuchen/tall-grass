package io.nicolaszurbuchen.tallgrass.core.ability.domain.fake

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * The abilities worth having on hand, chosen for the cases rather than for the names.
 *
 * Between them they cover an ability whose long entry says more than its short one, one whose long
 * entry is a verbatim repeat of it -- 46 of the 314 are -- a holder that has it in a normal slot, and
 * one that has it hidden.
 */
object AbilityFixtures {
    val levitate =
        Ability(
            slug = "levitate",
            name = "Levitate",
            generation = 3,
            shortEffect = "Evades Ground moves.",
        )

    val adaptability =
        Ability(
            slug = "adaptability",
            name = "Adaptability",
            generation = 4,
            shortEffect = "Increases the same-type attack bonus from 1.5× to 2×.",
        )

    /** Upstream had more to say, so the detail draws both sections. */
    val levitateDetail =
        AbilityDetail(
            slug = "levitate",
            name = "Levitate",
            generation = 3,
            shortEffect = "Evades Ground moves.",
            effect = "This Pokémon is immune to Ground-type moves, Spikes, Toxic Spikes and Arena Trap.",
        )

    /** The 46-row case: the long entry repeats the short one, so In depth has nothing to add. */
    val stenchDetail =
        AbilityDetail(
            slug = "stench",
            name = "Stench",
            generation = 3,
            shortEffect = "Has a 10% chance of making target Pokémon flinch with each hit.",
            effect = "Has a 10% chance of making target Pokémon flinch with each hit.",
        )

    val gastly =
        AbilityHolder(
            variantSlug = "gastly",
            cardSlug = "gastly",
            dexNumber = 92,
            name = "Gastly",
            artworkUrl = "https://example.invalid/gastly.png",
            primaryType = PokemonType.GHOST,
            secondaryType = PokemonType.POISON,
            isHidden = false,
        )

    val vibrava =
        AbilityHolder(
            variantSlug = "vibrava",
            cardSlug = "vibrava",
            dexNumber = 329,
            name = "Vibrava",
            artworkUrl = "https://example.invalid/vibrava.png",
            primaryType = PokemonType.GROUND,
            secondaryType = PokemonType.DRAGON,
            isHidden = true,
        )
}
