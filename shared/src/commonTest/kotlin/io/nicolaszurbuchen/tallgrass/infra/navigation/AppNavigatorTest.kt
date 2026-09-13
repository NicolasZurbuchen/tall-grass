package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigatorTest {
    // region navigateTo

    @Test
    fun navigateTo_beforeAttach_doesNothing() {
        val navigator = AppNavigator()

        navigator.navigateTo(TestKey.A)
    }

    @Test
    fun navigateTo_pushesKeyOntoAttachedBackStack() {
        val backStack = NavBackStack<NavKey>(TestKey.A)
        val navigator = AppNavigator().apply { attach(backStack) }

        navigator.navigateTo(TestKey.B)

        assertEquals(listOf(TestKey.A, TestKey.B), backStack.toList())
    }

    // endregion

    // region navigateBack

    @Test
    fun navigateBack_removesLastEntry() {
        val backStack = NavBackStack<NavKey>(TestKey.A, TestKey.B)
        val navigator = AppNavigator().apply { attach(backStack) }

        navigator.navigateBack()

        assertEquals(listOf(TestKey.A), backStack.toList())
    }

    @Test
    fun navigateBack_singleEntryStack_leavesStackEmpty() {
        val backStack = NavBackStack<NavKey>(TestKey.A)
        val navigator = AppNavigator().apply { attach(backStack) }

        navigator.navigateBack()

        assertEquals(emptyList(), backStack.toList())
    }

    @Test
    fun navigateBack_beforeAttach_doesNothing() {
        val navigator = AppNavigator()

        navigator.navigateBack()
    }

    // endregion

    // region popUpTo

    @Test
    fun popUpTo_matchingEntry_truncatesEverythingAfterIt() {
        val backStack = NavBackStack<NavKey>(TestKey.A, TestKey.B, TestKey.C)
        val navigator = AppNavigator().apply { attach(backStack) }

        navigator.popUpTo { it == TestKey.A }

        assertEquals(listOf(TestKey.A), backStack.toList())
    }

    @Test
    fun popUpTo_noMatchingEntry_leavesStackUnchanged() {
        val backStack = NavBackStack<NavKey>(TestKey.A, TestKey.B)
        val navigator = AppNavigator().apply { attach(backStack) }

        navigator.popUpTo { it == TestKey.C }

        assertEquals(listOf(TestKey.A, TestKey.B), backStack.toList())
    }

    @Test
    fun popUpTo_matchIsTopOfStack_leavesStackUnchanged() {
        val backStack = NavBackStack<NavKey>(TestKey.A, TestKey.B)
        val navigator = AppNavigator().apply { attach(backStack) }

        navigator.popUpTo { it == TestKey.B }

        assertEquals(listOf(TestKey.A, TestKey.B), backStack.toList())
    }

    @Test
    fun popUpTo_beforeAttach_doesNothing() {
        val navigator = AppNavigator()

        navigator.popUpTo { true }
    }

    // endregion

    private sealed interface TestKey : NavKey {
        @Serializable
        data object A : TestKey

        @Serializable
        data object B : TestKey

        @Serializable
        data object C : TestKey
    }
}
