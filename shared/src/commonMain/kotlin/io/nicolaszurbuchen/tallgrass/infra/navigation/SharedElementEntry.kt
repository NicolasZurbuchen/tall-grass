package io.nicolaszurbuchen.tallgrass.infra.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

/**
 * Marks a destination the host reaches from an element already on screen.
 *
 * **A screen that grows out of something the reader tapped must not also arrive from the side.** The
 * shared element is already saying where the screen came from; a slide says it came from somewhere
 * else at the same time, and the two readings fight. Such a screen cross-dissolves instead, and the
 * matched element carries the movement on its own.
 *
 * Carried as entry metadata rather than as a marker interface on the key, because it is a fact about
 * how the host draws the destination and not about the destination itself — and because navigation3
 * already hands metadata to the display for exactly this.
 */
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
