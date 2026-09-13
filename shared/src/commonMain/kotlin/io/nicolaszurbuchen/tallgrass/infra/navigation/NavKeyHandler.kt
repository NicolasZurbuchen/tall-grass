package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

interface NavKeyHandler {
    fun EntryProviderScope<NavKey>.registerEntries()
}
