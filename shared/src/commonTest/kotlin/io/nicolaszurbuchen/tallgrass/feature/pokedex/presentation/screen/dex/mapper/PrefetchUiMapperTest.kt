package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.mapper

import io.nicolaszurbuchen.tallgrass.infra.image.ImagePrefetchProgress
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PrefetchUiMapperTest {
    private fun progress(
        fetched: Int = 0,
        alreadyCached: Int = 0,
        failed: Int = 0,
        total: Int = 100,
        stoppedForSpace: Boolean = false,
    ) = ImagePrefetchProgress(fetched, alreadyCached, failed, total, stoppedForSpace)

    @Test
    fun toUiModel_saysNothingAboutAFinishedRun() {
        // The expected outcome, and the reader was never waiting for it. A banner announcing that
        // something they did not ask for has succeeded is noise.
        assertNull(progress(fetched = 100).toUiModel())
    }

    @Test
    fun toUiModel_saysNothingWhenEverythingWasAlreadyOnDisk() {
        // The second visit to the dex. Nothing was downloaded, so there is nothing to report.
        assertNull(progress(alreadyCached = 100).toUiModel())
    }

    @Test
    fun toUiModel_showsABarWhileTheRunIsGoing() {
        val ui = assertNotNull(progress(fetched = 42).toUiModel())

        assertEquals(0.42f, ui.fraction)
    }

    @Test
    fun toUiModel_roundsThePercentageDownRatherThanShowingAFullBarEarly() {
        val ui = assertNotNull(progress(fetched = 999, total = 1000).toUiModel())

        assertEquals(listOf("99"), (ui.message as UiText.Resource).args)
    }

    @Test
    fun toUiModel_replacesTheBarWithASentenceWhenTheDiskFilledUp() {
        // The one outcome worth telling someone about, and the only one where the bar would be a
        // lie: it is not going to fill.
        val ui = assertNotNull(progress(fetched = 12, stoppedForSpace = true).toUiModel())

        assertNull(ui.fraction)
    }
}
