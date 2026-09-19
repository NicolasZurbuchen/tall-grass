package io.nicolaszurbuchen.tallgrass.design.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MotionTest {
    @Test
    fun delayFor_stepsOnceEachForTheFirstItems() {
        assertEquals(0, AppStagger.delayFor(0))
        assertEquals(55, AppStagger.delayFor(1))
        assertEquals(385, AppStagger.delayFor(7))
    }

    @Test
    fun delayFor_capsSoADeepListEntersAsFastAsAShallowOne() {
        // The whole reason the cap exists. Uncapped, the dex's 1,082nd card would wait a minute.
        assertEquals(AppStagger.delayFor(7), AppStagger.delayFor(8))
        assertEquals(AppStagger.delayFor(7), AppStagger.delayFor(1081))
    }

    @Test
    fun delayFor_treatsAnIndexBeforeTheViewportAsTheFirstItem() {
        // A LazyList reports a negative offset mid-scroll if an item is asked about before it is
        // laid out. A negative delay is not a thing an animation can take.
        assertEquals(0, AppStagger.delayFor(-1))
    }

    @Test
    fun aFullEntrance_staysUnderHalfASecond() {
        // The number #12 committed to. It is the product of the two constants above, so this fails
        // if either is changed without the other being reconsidered.
        val total = AppStagger.delayFor(Int.MAX_VALUE) + AppDuration.INSTANT

        assertTrue(total < 550, "A full entrance takes ${total}ms")
    }
}
