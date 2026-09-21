package io.nicolaszurbuchen.tallgrass.core.move.domain.fake

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.BattleStat
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveAilment
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveCategory
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveMeta
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveStatChange
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveTarget
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * The four moves worth having on hand, chosen for the cases rather than for the names.
 *
 * Between them they cover a damaging move with a chance, a status move with none of the numbers, a
 * guaranteed ailment, and a move upstream has recorded nothing mechanical about.
 */
object MoveFixtures {
    val flamethrower =
        Move(
            slug = "flamethrower",
            name = "Flamethrower",
            type = PokemonType.FIRE,
            damageClass = DamageClass.SPECIAL,
            power = 90,
        )

    val thunderWave =
        Move(
            slug = "thunder-wave",
            name = "Thunder Wave",
            type = PokemonType.ELECTRIC,
            damageClass = DamageClass.STATUS,
            power = null,
        )

    /** A chance that is stated: burns one time in ten. */
    val flamethrowerDetail =
        MoveDetail(
            slug = "flamethrower",
            name = "Flamethrower",
            generation = 1,
            type = PokemonType.FIRE,
            damageClass = DamageClass.SPECIAL,
            power = 90,
            accuracy = 100,
            pp = 15,
            priority = 0,
            target = MoveTarget.SELECTED_POKEMON,
            shortEffect = "Has a 10% chance to burn the target.",
            effect = "Inflicts regular damage. Has a 10% chance to burn the target.",
            meta =
                MoveMeta(
                    category = MoveCategory.DAMAGE_AILMENT,
                    ailment = MoveAilment.BURN,
                    ailmentChance = 10,
                    minHits = null,
                    maxHits = null,
                    minTurns = null,
                    maxTurns = null,
                    drain = null,
                    healing = null,
                    critRate = null,
                    flinchChance = null,
                    statChance = null,
                ),
            statChanges = emptyList(),
        )

    /**
     * **The move the whole zero-means-always rule exists for.** Upstream stores `ailment_chance = 0`
     * here and it means the paralysis is certain, not impossible.
     */
    val thunderWaveDetail =
        MoveDetail(
            slug = "thunder-wave",
            name = "Thunder Wave",
            generation = 1,
            type = PokemonType.ELECTRIC,
            damageClass = DamageClass.STATUS,
            power = null,
            accuracy = 90,
            pp = 20,
            priority = 0,
            target = MoveTarget.SELECTED_POKEMON,
            shortEffect = "Paralyzes the target.",
            effect = "Paralyzes the target.",
            meta =
                MoveMeta(
                    category = MoveCategory.AILMENT,
                    ailment = MoveAilment.PARALYSIS,
                    ailmentChance = null,
                    minHits = null,
                    maxHits = null,
                    minTurns = null,
                    maxTurns = null,
                    drain = null,
                    healing = null,
                    critRate = null,
                    flinchChance = null,
                    statChance = null,
                ),
            statChanges = emptyList(),
        )

    /** A guaranteed stat change, which is 60 of the 174 moves that move a stat. */
    val growlDetail =
        thunderWaveDetail.copy(
            slug = "growl",
            name = "Growl",
            type = PokemonType.NORMAL,
            target = MoveTarget.ALL_OPPONENTS,
            accuracy = 100,
            pp = 40,
            shortEffect = "Lowers the target's Attack by one stage.",
            effect = "Lowers the target's Attack by one stage.",
            meta =
                MoveMeta(
                    category = MoveCategory.NET_GOOD_STATS,
                    ailment = null,
                    ailmentChance = null,
                    minHits = null,
                    maxHits = null,
                    minTurns = null,
                    maxTurns = null,
                    drain = null,
                    healing = null,
                    critRate = null,
                    flinchChance = null,
                    statChance = null,
                ),
            statChanges = listOf(MoveStatChange(BattleStat.ATTACK, -1)),
        )

    /** One Pokemon that learns Flamethrower, and the row the champions regression was found on. */
    val charizardLearner =
        MoveLearner(
            variantSlug = "charizard",
            dexNumber = 6,
            name = "Charizard",
            formLabel = null,
            artworkUrl = "https://example.invalid/6.png",
            primaryType = PokemonType.FIRE,
            secondaryType = PokemonType.FLYING,
            method = LearnMethod.LEVEL_UP,
            level = 46,
        )

    /** One of the 92 Generation VIII and IX moves upstream has recorded no mechanics for. */
    val direClawDetail =
        MoveDetail(
            slug = "dire-claw",
            name = "Dire Claw",
            generation = 8,
            type = PokemonType.POISON,
            damageClass = DamageClass.PHYSICAL,
            power = 80,
            accuracy = 100,
            pp = 15,
            priority = 0,
            target = MoveTarget.SELECTED_POKEMON,
            shortEffect = null,
            effect = null,
            meta = null,
            statChanges = emptyList(),
        )
}
