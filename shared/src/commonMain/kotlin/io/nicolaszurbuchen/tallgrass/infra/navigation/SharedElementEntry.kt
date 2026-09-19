package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

// Marks a destination the host reaches from an element already on screen, so its motion can be
// chosen accordingly. Carried as entry metadata because it is a fact about how the host draws the
// destination rather than about the destination itself.
// DECISIONS.md § A screen reached from an element does not also arrive from the side
object SharedElementEntry {
    private const val KEY = "tallgrass.navigation.sharedElement"

    /** Pass to `entry<T>(metadata = ...)` for a destination reached from an element on screen. */
    val metadata: Map<String, Any> = mapOf(KEY to true)

    /**
     * Whether [scene] shows such a destination on top.
     *
     * The topmost entry is the one being animated to or from; the ones under it are the back stack
     * and have no say in the motion.
     */
    fun marks(scene: Scene<NavKey>): Boolean = scene.entries.lastOrNull()?.metadata?.containsKey(KEY) == true
}
