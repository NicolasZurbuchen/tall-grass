package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveMeta
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveFactUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_certain_suffix
import tallgrass.shared.generated.resources.move_detail_chance_suffix
import tallgrass.shared.generated.resources.move_detail_condition
import tallgrass.shared.generated.resources.move_detail_crit_rate
import tallgrass.shared.generated.resources.move_detail_drain
import tallgrass.shared.generated.resources.move_detail_hits
import tallgrass.shared.generated.resources.move_detail_hp_cost
import tallgrass.shared.generated.resources.move_detail_kind
import tallgrass.shared.generated.resources.move_detail_recoil
import tallgrass.shared.generated.resources.move_detail_times_range
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoveContentUiMapperTest {
    private val noMeta =
        MoveMeta(
            category = MoveFixtures.flamethrowerDetail.meta!!.category,
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
        )

    private fun labelOf(fact: MoveFactUiModel) = (fact.label as UiText.Resource).id

    private fun factWith(label: Any) = { facts: List<MoveFactUiModel> -> facts.single { labelOf(it) == label } }

    // region battle data

    @Test
    fun toUiModel_carriesTheIdentityTheHeroDraws() {
        val model = MoveFixtures.flamethrowerDetail.toUiModel()

        assertEquals("Flamethrower", model.name)
        assertEquals(TypeUiModel.FIRE, model.type)
        assertEquals(DamageClassUiModel.SPECIAL, model.damageClass)
        assertEquals(listOf(15), (model.ppText as UiText.Resource).args)
    }

    @Test
    fun toUiModel_drawsTheThreeBarsInTheOrderTheyAreRead() {
        val bars = MoveFixtures.flamethrowerDetail.toUiModel().bars

        assertEquals(listOf("90", "100%", "15"), bars.map { it.valueText })
        assertEquals(0.6f, bars[0].fraction)
        assertEquals(1f, bars[1].fraction)
    }

    @Test
    fun toUiModel_drawsAnAbsentFigureAsADashWithAnEmptyLane() {
        // The pair matters: a dash beside a full lane would be worse than either alone.
        val power = MoveFixtures.thunderWaveDetail.toUiModel().bars.first()

        assertEquals("—", power.valueText)
        assertEquals(0f, power.fraction)
    }

    @Test
    fun toUiModel_clampsAPowerAboveTheScaleRatherThanOverflowingTheLane() {
        // Explosion reaches 250 against a lane that is full at 150. Thirty-seven moves are over it.
        val explosion = MoveFixtures.flamethrowerDetail.copy(power = 250)

        assertEquals(1f, explosion.toUiModel().bars.first().fraction)
        assertEquals("250", explosion.toUiModel().bars.first().valueText)
    }

    @Test
    fun toUiModel_signsPriorityOnlyWhenItIsPositive() {
        // The scale runs -7 to +5 and zero is by far the commonest, so "+0" would be noise on almost
        // every move while "-7" has to keep its sign.
        assertEquals("0", MoveFixtures.flamethrowerDetail.toUiModel().priorityText)
        assertEquals("+1", MoveFixtures.flamethrowerDetail.copy(priority = 1).toUiModel().priorityText)
        assertEquals("-7", MoveFixtures.flamethrowerDetail.copy(priority = -7).toUiModel().priorityText)
    }

    // endregion

    // region mechanics

    @Test
    fun toUiModel_hasNoMechanicsAtAllForAMoveUpstreamHasNotFilledIn() {
        // 92 Generation VIII and IX moves. An empty list is what leaves the section out rather than
        // drawing an empty card.
        val model = MoveFixtures.direClawDetail.toUiModel()

        assertTrue(model.facts.isEmpty())
        assertNull(model.effect, "and no effect paragraph either")
    }

    @Test
    fun toUiModel_alwaysLeadsWithTheKind() {
        // The one meta field every filled-in move has, and the only classification in this app that
        // was not invented.
        val facts = MoveFixtures.flamethrowerDetail.toUiModel().facts

        assertEquals(Res.string.move_detail_kind, labelOf(facts.first()))
        assertEquals(UiText.Raw("Damage and a condition"), facts.first().value)
    }

    @Test
    fun toUiModel_statesAnAilmentsChanceWhenThereIsOne() {
        val condition = factWith(Res.string.move_detail_condition)(MoveFixtures.flamethrowerDetail.toUiModel().facts)

        val parts = (condition.value as UiText.Composite).parts
        assertEquals(UiText.Raw("Burn"), parts[0])
        assertEquals(Res.string.move_detail_chance_suffix, (parts[1] as UiText.Resource).id)
        assertEquals(listOf(10), (parts[1] as UiText.Resource).args)
    }

    /**
     * **The whole point of the dataset work, asserted.**
     *
     * Upstream stores `ailment_chance = 0` on the thirty-six moves whose ailment is certain. Read
     * literally it says Thunder Wave paralyses 0% of the time. The generator drops it and this is
     * where the null becomes the word "always" rather than a number.
     */
    @Test
    fun toUiModel_saysAlwaysRatherThanZeroForAGuaranteedAilment() {
        val condition = factWith(Res.string.move_detail_condition)(MoveFixtures.thunderWaveDetail.toUiModel().facts)

        val parts = (condition.value as UiText.Composite).parts
        assertEquals(UiText.Raw("Paralysis"), parts[0])
        assertEquals(Res.string.move_detail_certain_suffix, (parts[1] as UiText.Resource).id)
        assertTrue((parts[1] as UiText.Resource).args.isEmpty(), "no percentage to substitute")
    }

    @Test
    fun toUiModel_givesAStatChangeItsOwnRowAndTheSameCertaintyRule() {
        // Growl always lowers Attack, which is 60 of the 174 moves that move a stat.
        val facts = MoveFixtures.growlDetail.toUiModel().facts
        val attack = facts.single { it.label == UiText.Raw("Attack") }

        val parts = (attack.value as UiText.Composite).parts
        assertEquals(listOf("-1"), (parts[0] as UiText.Resource).args)
        assertEquals(Res.string.move_detail_certain_suffix, (parts[1] as UiText.Resource).id)
    }

    @Test
    fun toUiModel_labelsANegativeDrainAsRecoil() {
        // The sign is the meaning, and "Drain -33%" would make a reader work it out. Absolute value
        // under a label that already says the direction.
        val recoiling = MoveFixtures.flamethrowerDetail.copy(meta = noMeta.copy(drain = -33))
        val draining = MoveFixtures.flamethrowerDetail.copy(meta = noMeta.copy(drain = 50))

        val recoil = factWith(Res.string.move_detail_recoil)(recoiling.toUiModel().facts)
        val drain = factWith(Res.string.move_detail_drain)(draining.toUiModel().facts)

        assertEquals(listOf(33), (recoil.value as UiText.Resource).args)
        assertEquals(listOf(50), (drain.value as UiText.Resource).args)
    }

    @Test
    fun toUiModel_labelsNegativeHealingAsACost() {
        // Struggle and Clangorous Soul. Two moves, and the only ones where "Heals -25%" would be an
        // outright false statement.
        val struggle = MoveFixtures.flamethrowerDetail.copy(meta = noMeta.copy(healing = -25))

        val cost = factWith(Res.string.move_detail_hp_cost)(struggle.toUiModel().facts)
        assertEquals(listOf(25), (cost.value as UiText.Resource).args)
    }

    @Test
    fun toUiModel_readsAMultiHitRangeAsARange() {
        val doubleKick = MoveFixtures.flamethrowerDetail.copy(meta = noMeta.copy(minHits = 2, maxHits = 5))

        val hits = factWith(Res.string.move_detail_hits)(doubleKick.toUiModel().facts)
        assertEquals(Res.string.move_detail_times_range, (hits.value as UiText.Resource).id)
        assertEquals(listOf(2, 5), (hits.value as UiText.Resource).args)
    }

    @Test
    fun toUiModel_signsTheCriticalHitBonus() {
        val slash = MoveFixtures.flamethrowerDetail.copy(meta = noMeta.copy(critRate = 1))

        val crit = factWith(Res.string.move_detail_crit_rate)(slash.toUiModel().facts)
        assertEquals(listOf("+1"), (crit.value as UiText.Resource).args)
    }

    // endregion
}
