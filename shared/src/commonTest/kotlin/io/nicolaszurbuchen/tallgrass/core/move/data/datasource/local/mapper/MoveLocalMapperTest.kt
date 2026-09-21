package io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoveStatChanges
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoves
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.BattleStat
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveAilment
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveCategory
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveStatChange
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveTarget
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.Move as MoveRow

class MoveLocalMapperTest {
    private fun listRow(
        slug: String = "flamethrower",
        typeSlug: String = "fire",
        damageClass: String = "special",
        power: Long? = 90,
    ) = SelectMoves(
        slug = slug,
        name = "Flamethrower",
        typeSlug = typeSlug,
        damageClass = damageClass,
        power = power,
    )

    @Suppress("LongParameterList")
    private fun detailRow(
        typeSlug: String = "fire",
        damageClass: String = "special",
        target: String = "selected-pokemon",
        power: Long? = 90,
        accuracy: Long? = 100,
        pp: Long? = 15,
        effect: String? = "Inflicts regular damage.",
        category: String? = "damage-ailment",
        ailment: String? = "burn",
        ailmentChance: Long? = 10,
        drain: Long? = null,
        critRate: Long? = null,
    ) = MoveRow(
        slug = "flamethrower",
        name = "Flamethrower",
        generation = 1,
        typeSlug = typeSlug,
        damageClass = damageClass,
        power = power,
        accuracy = accuracy,
        pp = pp,
        priority = 0,
        target = target,
        shortEffect = "Has a 10% chance to burn the target.",
        effect = effect,
        category = category,
        ailment = ailment,
        ailmentChance = ailmentChance,
        minHits = null,
        maxHits = null,
        minTurns = null,
        maxTurns = null,
        drain = drain,
        healing = null,
        critRate = critRate,
        flinchChance = null,
        statChance = null,
    )

    @Test
    fun toDomain_carriesEveryFieldACardDraws() {
        val move = listRow().toDomain()

        assertEquals("flamethrower", move?.slug)
        assertEquals("Flamethrower", move?.name)
        assertEquals(PokemonType.FIRE, move?.type)
        assertEquals(DamageClass.SPECIAL, move?.damageClass)
        assertEquals(90, move?.power)
    }

    @Test
    fun toDomain_treatsAMissingPowerAsOrdinary() {
        // A third of the list is status moves. Absent here is the normal case, not a failure.
        assertNull(listRow(power = null).toDomain()?.power)
    }

    @Test
    fun toDomain_dropsARowWhoseTypeOrDamageClassIsUnknown() {
        // A disagreement between the bundled dataset and this build. Dropping the row is better than
        // a card with no colour or a fourth damage class nothing can draw.
        assertNull(listRow(typeSlug = "stellar").toDomain())
        assertNull(listRow(damageClass = "shadow").toDomain())
    }

    @Test
    fun toDomain_readsTheWholeMoveIncludingItsMeta() {
        val move = detailRow().toDomain(emptyList())

        assertEquals(MoveTarget.SELECTED_POKEMON, move?.target)
        assertEquals(MoveCategory.DAMAGE_AILMENT, move?.meta?.category)
        assertEquals(MoveAilment.BURN, move?.meta?.ailment)
        assertEquals(10, move?.meta?.ailmentChance)
    }

    @Test
    fun toDomain_keepsAGuaranteedAilmentsChanceNull() {
        // Thunder Wave's case. Upstream stores 0 and the generator drops it, so a null here beside a
        // real ailment has to survive the read meaning "always" rather than becoming 0 again.
        val move = detailRow(ailment = "paralysis", ailmentChance = null).toDomain(emptyList())

        assertEquals(MoveAilment.PARALYSIS, move?.meta?.ailment)
        assertNull(move?.meta?.ailmentChance)
    }

    @Test
    fun toDomain_hasNoMetaAtAllWhenTheCategoryIsMissing() {
        // The twelve meta columns are null together, and the 92 recent moves upstream has not filled
        // in are the reason the whole object is nullable rather than each field defaulted.
        val move = detailRow(category = null, ailment = null, ailmentChance = null).toDomain(emptyList())

        assertNull(move?.meta)
        assertEquals("flamethrower", move?.slug, "the move itself still reads")
    }

    @Test
    fun toDomain_dropsADetailRowWhoseTargetIsUnknown() {
        assertNull(detailRow(target = "adjacent-ally-or-self").toDomain(emptyList()))
    }

    @Test
    fun toDomain_ordersStatChangesTheWayStatsAreDrawnRatherThanHowTheyArrive() {
        // The table has no ordering column, so its rows come back by primary key -- alphabetically.
        // BattleStat's declaration order is the one every other screen uses.
        val rows =
            listOf(
                SelectMoveStatChanges(statSlug = "speed", change = 1),
                SelectMoveStatChanges(statSlug = "attack", change = 1),
                SelectMoveStatChanges(statSlug = "special-defense", change = 1),
            )

        val move = detailRow().toDomain(rows)

        assertEquals(
            listOf(
                MoveStatChange(BattleStat.ATTACK, 1),
                MoveStatChange(BattleStat.SPECIAL_DEFENSE, 1),
                MoveStatChange(BattleStat.SPEED, 1),
            ),
            move?.statChanges,
        )
    }

    @Test
    fun toDomain_dropsAStatChangeNamingAStatThisBuildDoesNotKnow() {
        val rows = listOf(SelectMoveStatChanges(statSlug = "hp", change = 1))

        assertTrue(detailRow().toDomain(rows)?.statChanges.orEmpty().isEmpty())
    }

    @Test
    fun toDomain_keepsDrainSigned() {
        // The sign is the whole meaning: negative is recoil, and an absolute value would say a move
        // that hurts its user heals it.
        assertEquals(-33, detailRow(drain = -33).toDomain(emptyList())?.meta?.drain)
        assertEquals(50, detailRow(drain = 50).toDomain(emptyList())?.meta?.drain)
    }
}
