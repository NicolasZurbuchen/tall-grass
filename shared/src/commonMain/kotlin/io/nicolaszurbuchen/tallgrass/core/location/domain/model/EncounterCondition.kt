package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One state an encounter table can be conditioned on: a time of day, a season, a weather, whether a
 * swarm is on, which radio station is playing.
 *
 * [axis] is the question and the slug is one answer to it -- `time` is the axis, `time-night` the
 * value. The selector draws one control per axis that a table actually varies on, so a route with no
 * conditions shows none and HeartGold's Route 1 shows three.
 *
 * [isDefault] marks the state the games treat as ordinary. It is **not** the screen's "Any", which
 * means unpinned and shows the best case across every state; that is a further option beside these
 * rather than one of them.
 */
data class EncounterCondition(
    val slug: String,
    val axis: String,
    val isDefault: Boolean,
)
