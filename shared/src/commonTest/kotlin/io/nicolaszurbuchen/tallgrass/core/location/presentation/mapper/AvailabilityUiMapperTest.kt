package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Console
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.ConsoleUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvailabilityUiMapperTest {
    private val versions =
        listOf(LocationFixtures.red, LocationFixtures.heartgold, LocationFixtures.pokemonY)

    @Test
    fun rowsAreDataRatherThanAFixedFive() {
        // A console with no games here gets no row at all, which is what gives Kanto twelve cells
        // and no 3DS row. #24 is explicit that this is derived rather than hardcoded.
        val grid = listOf(LocationFixtures.red).toAvailabilityGridUiModel(emptySet())

        assertEquals(listOf(ConsoleUiModel.GB_GBC), grid.rows.map { it.console })
    }

    @Test
    fun rowsRunNewestMachineFirst() {
        // The row a reader is most likely to be holding belongs at the top, and the order is the
        // domain enum's rather than the order the games arrived in.
        val grid = versions.toAvailabilityGridUiModel(emptySet())

        assertEquals(listOf(ConsoleUiModel.THREE_DS, ConsoleUiModel.DS, ConsoleUiModel.GB_GBC), grid.rows.map { it.console })
    }

    @Test
    fun aCellIsGreenOnlyWhenTheVersionIsInTheSet() {
        val grid = versions.toAvailabilityGridUiModel(setOf("heartgold"))
        val cells = grid.rows.flatMap { it.cells }.associateBy { it.slug }

        assertTrue(cells.getValue("heartgold").isAvailable)
        assertFalse(cells.getValue("red").isAvailable)
    }

    @Test
    fun everyCellSurvivesIncludingTheGreyOnes() {
        // A grey cell has to be there to be tapped: that is how a negative gets confirmed, and how
        // the two empty states are told apart at the moment somebody asks. See #9.
        val grid = versions.toAvailabilityGridUiModel(emptySet())

        assertEquals(3, grid.rows.sumOf { it.cells.size })
    }

    @Test
    fun codesTravelUnchangedBecauseTheirRowDisambiguatesThem() {
        // Yellow and Pokemon Y both carry "Y" and are told apart by their row, which is the whole
        // job of the grouping.
        val grid = versions.toAvailabilityGridUiModel(emptySet())
        val threeDs = grid.rows.single { it.console == ConsoleUiModel.THREE_DS }

        assertEquals("Y", threeDs.cells.single().code)
    }

    @Test
    fun everyConsole_hasARowLabel() {
        // Exhaustive by construction, so this cannot fail without one enum having gained a member
        // the other has not.
        assertEquals(Console.entries.size, ConsoleUiModel.entries.size)
        Console.entries.forEach { assertEquals(it.name, it.toUiModel().name) }
    }
}
