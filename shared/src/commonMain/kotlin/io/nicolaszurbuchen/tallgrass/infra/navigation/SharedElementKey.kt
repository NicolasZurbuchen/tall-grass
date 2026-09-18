package io.nicolaszurbuchen.tallgrass.infra.navigation

import kotlinx.serialization.Serializable

/**
 * Identifies one shared element on both sides of a navigation boundary.
 *
 * [source] is what stops two screens showing the same thing from claiming the same key. Both are
 * composed at once while the host cross-fades between them, so with [id] alone a grid card and a
 * search result for the same subject would match and animate a transition nobody asked for.
 *
 * Serializable because it travels inside the destination: the arriving screen has to reproduce the
 * key of whatever sent it, and that has to survive process death along with the back stack.
 */
@Serializable
data class SharedElementKey(
    val source: String,
    val id: String,
)
