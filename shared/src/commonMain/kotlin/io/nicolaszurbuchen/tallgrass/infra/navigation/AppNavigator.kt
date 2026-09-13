package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class AppNavigator {
    private var backStack: NavBackStack<NavKey>? = null

    fun attach(backStack: NavBackStack<NavKey>) {
        this.backStack = backStack
    }

    fun navigateTo(key: NavKey) {
        backStack?.add(key)
    }

    fun navigateBack() {
        backStack?.removeLastOrNull()
    }

    fun popUpTo(predicate: (NavKey) -> Boolean) {
        val stack = backStack ?: return
        val index = stack.indexOfFirst(predicate)
        if (index != -1) {
            while (stack.size > index + 1) {
                stack.removeAt(stack.size - 1)
            }
        }
    }
}
