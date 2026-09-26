package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EncounterMethodUiMapperTest {
    @Test
    fun aCuratedMethod_getsItsWrittenLabel() {
        // "Tall grass" rather than "Walk", which #9 called out as wrong twice over: the wrong
        // granularity, and less evocative than what the games call the place it happens in.
        val walk = "walk".toEncounterMethodUiModel()

        assertEquals("walk", walk.slug)
        assertTrue(walk.label is UiText.Resource)
    }

    @Test
    fun anUnknownMethod_isSpelledOutOfItsSlug() {
        // Twenty of the 64 methods in the dataset land here, and a method upstream adds later will
        // too. A fallback is not a failure: the slug opened out still reads.
        assertEquals(UiText.Raw("Sky battle"), "sky-battle".toEncounterMethodUiModel().label)
    }

    @Test
    fun theFallback_isSentenceCaseRatherThanTitleCase() {
        // Title-casing would give "Sky Battle", which reads as a proper noun upstream did not write.
        assertEquals(UiText.Raw("Fishing in deep water"), "fishing-in-deep-water".toEncounterMethodUiModel().label)
    }
}
