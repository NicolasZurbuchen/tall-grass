package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_level_one
import tallgrass.shared.generated.resources.location_detail_level_range
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Built from HeartGold's Route 1, which is the table the correction on #8 was found on.
 *
 * The five rows below are real fragments of one twelve-slot walking table, each tagged with the state
 * that switches it on. Added up they are 100% of nothing in particular; read as AND-filters and
 * pinned to morning, no swarm and the radio off, they are exactly 100%.
 */
class EncounterTableUiMapperTest {
    private val conditions =
        listOf(
            EncounterCondition("time-morning", "time", isDefault = false),
            EncounterCondition("time-night", "time", isDefault = false),
            EncounterCondition("swarm-no", "swarm", isDefault = true),
            EncounterCondition("swarm-yes", "swarm", isDefault = false),
            EncounterCondition("radio-off", "radio", isDefault = true),
            EncounterCondition("radio-hoenn", "radio", isDefault = false),
        )

    private val table =
        listOf(
            row("pidgey", chance = 20, conditions = listOf("swarm-no", "time-morning")),
            row("rattata", chance = 30, conditions = listOf("radio-off", "time-morning")),
            row("sentret", chance = 20, conditions = listOf("swarm-no")),
            row("hoothoot", chance = 20, conditions = listOf("time-morning")),
            row("furret", chance = 10, conditions = listOf("radio-off")),
            // Three rows belonging to other states. They are what makes each axis a real choice --
            // an axis with one value is not one -- and none of them may be counted into the state
            // above.
            row("poochyena", chance = 40, conditions = listOf("swarm-yes")),
            row("hoothoot", chance = 45, conditions = listOf("time-night")),
            row("shinx", chance = 40, conditions = listOf("radio-hoenn")),
        )

    @Test
    fun aCompleteState_sumsToExactlyOneHundred() {
        val pinned = mapOf("time" to "time-morning", "swarm" to "swarm-no", "radio" to "radio-off")
        val rows = table.toEncounterAreasUiModel(conditions, pinned).single().rows

        assertEquals(100, rows.sumOf { (it.rateFraction * 100).toInt() })
        assertEquals(listOf("rattata", "hoothoot", "pidgey", "sentret", "furret"), rows.map { it.variantSlug })
    }

    @Test
    fun aCompleteState_leavesOutTheRowsItExcludes() {
        // The swarm row is a different state, not a smaller share of this one.
        val pinned = mapOf("time" to "time-morning", "swarm" to "swarm-no", "radio" to "radio-off")
        val rows = table.toEncounterAreasUiModel(conditions, pinned).single().rows

        assertTrue(rows.none { it.variantSlug == "poochyena" })
    }

    @Test
    fun anUnpinnedAxis_satisfiesAllOfItsValuesAtOnce() {
        // Which is what "Any" means: nothing is excluded. Eight rows come back as seven lines,
        // because Hoothoot appears in two states and folds to its best one.
        val rows = table.toEncounterAreasUiModel(conditions, emptyMap()).single().rows

        assertEquals(table.map { it.variantSlug }.distinct().size, rows.size)
        assertEquals(0.45f, rows.single { it.variantSlug == "hoothoot" }.rateFraction)
    }

    @Test
    fun anythingUnpinned_makesTheFiguresABestCase() {
        val rows = table.toEncounterAreasUiModel(conditions, mapOf("time" to "time-morning")).single().rows

        assertTrue(rows.all { it.isBestCase })
    }

    @Test
    fun everyAxisPinned_makesTheFiguresExact() {
        val pinned = mapOf("time" to "time-morning", "swarm" to "swarm-no", "radio" to "radio-off")
        val rows = table.toEncounterAreasUiModel(conditions, pinned).single().rows

        assertTrue(rows.none { it.isBestCase })
    }

    @Test
    fun aVariantInSeveralStates_isOneLineAtItsBestRate() {
        // Pidgey at 45% by day and 20% at night is "up to 45%", not 65%: those are alternatives that
        // cannot both happen, and summing them would invent a rate.
        val split =
            listOf(
                row("pidgey", chance = 45, conditions = listOf("time-morning")),
                row("pidgey", chance = 20, conditions = listOf("time-night")),
            )

        val rows = split.toEncounterAreasUiModel(conditions, emptyMap()).single().rows

        assertEquals(1, rows.size)
        assertEquals(0.45f, rows.single().rateFraction)
    }

    @Test
    fun aVariantInSeveralSlotsOfOneState_isSummed() {
        // Those are slots of one table rather than alternatives, and upstream stores a Pokemon
        // holding four of twelve as four rows.
        val slots =
            listOf(
                row("pidgey", chance = 30, conditions = listOf("time-morning")),
                row("pidgey", chance = 15, conditions = listOf("time-morning")),
            )

        assertEquals(0.45f, slots.toEncounterAreasUiModel(conditions, emptyMap()).single().rows.single().rateFraction)
    }

    @Test
    fun aMethodWithNoMeaningfulRate_printsNoFigure() {
        // Raids, gifts, trades and SOS calls carry a zero, and the row says nothing rather than 0%.
        val gift = listOf(row("togepi", chance = 0, conditions = emptyList()))
        val rendered = gift.toEncounterAreasUiModel(conditions, emptyMap()).single().rows.single()

        assertNull(rendered.rateText)
        assertEquals(0f, rendered.rateFraction)
    }

    @Test
    fun levels_collapseToARangeOrASingleNumber() {
        val single = listOf(row("pidgey", chance = 10, conditions = emptyList(), minLevel = 3, maxLevel = 3))

        assertEquals(
            UiText.Resource(Res.string.location_detail_level_one, listOf(3)),
            single.toEncounterAreasUiModel(conditions, emptyMap()).single().rows.single().levelText,
        )

        val range = listOf(row("pidgey", chance = 10, conditions = emptyList(), minLevel = 2, maxLevel = 4))

        assertEquals(
            UiText.Resource(Res.string.location_detail_level_range, listOf(2, 4)),
            range.toEncounterAreasUiModel(conditions, emptyMap()).single().rows.single().levelText,
        )
    }

    @Test
    fun areas_areNamedOnlyWhenThereIsMoreThanOne() {
        // The fold #24 asks for is invisible in the 85.5% of cases with a single area, and the name
        // appears exactly where it is needed to keep two denominators apart.
        val oneArea = table.toEncounterAreasUiModel(conditions, emptyMap())

        assertEquals(1, oneArea.size)
        assertNull(oneArea.single().name)

        val twoAreas =
            listOf(
                row("magikarp", chance = 100, conditions = emptyList(), area = "canalave-city", areaName = null),
                row("magikarp", chance = 100, conditions = emptyList(), area = "canalave-city-sea", areaName = "Sea"),
            ).toEncounterAreasUiModel(conditions, emptyMap())

        assertEquals(2, twoAreas.size)
        assertEquals(listOf(null, "Sea"), twoAreas.map { it.name })
    }

    @Test
    fun axes_areOnlyTheOnesTheTableVariesOn() {
        // A control offering one answer is asking a question nobody has, so an axis with a single
        // value is left out. FireRed shows none of these and HeartGold shows three.
        val axes = table.toConditionAxesUiModel(conditions, emptyMap())

        assertEquals(listOf("radio", "swarm", "time"), axes.map { it.axis })
    }

    @Test
    fun anAxisWithOneValue_getsNoControl() {
        val single = listOf(row("pidgey", chance = 10, conditions = listOf("time-morning")))

        assertTrue(single.toConditionAxesUiModel(conditions, emptyMap()).isEmpty())
    }

    @Test
    fun anOptionIsLabelledWithoutItsAxisInFront() {
        val axes = table.toConditionAxesUiModel(conditions, emptyMap())
        val time = axes.single { it.axis == "time" }

        assertEquals(UiText.Raw("Time"), time.label)
        assertEquals(listOf(UiText.Raw("Morning"), UiText.Raw("Night")), time.options.map { it.label })
    }

    @Test
    fun anAxisCarriesWhicheverValueIsPinned() {
        val axes = table.toConditionAxesUiModel(conditions, mapOf("time" to "time-night"))

        assertEquals("time-night", axes.single { it.axis == "time" }.selected)
        assertNull(axes.single { it.axis == "swarm" }.selected)
    }

    @Test
    fun theBarIsAShareOfAHundredRatherThanOfTheLargestRow() {
        // Stretching it to fill would say the commonest Pokemon on a route is a certainty.
        val thin = listOf(row("chansey", chance = 1, conditions = emptyList()))

        assertEquals(0.01f, thin.toEncounterAreasUiModel(conditions, emptyMap()).single().rows.single().rateFraction)
        assertFalse(thin.toEncounterAreasUiModel(conditions, emptyMap()).single().rows.single().rateFraction == 1f)
    }

    private fun row(
        variant: String,
        chance: Int,
        conditions: List<String>,
        minLevel: Int = 2,
        maxLevel: Int = 4,
        area: String = "kanto-route-1",
        areaName: String? = null,
    ) = Encounter(
        id = variant.hashCode().toLong() + chance + conditions.size,
        variantSlug = variant,
        cardSlug = variant,
        dexNumber = 16,
        name = variant.replaceFirstChar { it.uppercase() },
        artworkUrl = "$variant.png",
        primaryType = PokemonType.NORMAL,
        secondaryType = null,
        areaSlug = area,
        areaName = areaName,
        method = "walk",
        minLevel = minLevel,
        maxLevel = maxLevel,
        chance = chance,
        conditions = conditions,
    )
}
