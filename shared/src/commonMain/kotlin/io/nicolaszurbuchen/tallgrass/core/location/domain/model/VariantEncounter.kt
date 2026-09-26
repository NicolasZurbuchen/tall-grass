package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One place a Pokemon can be met in one game, for the Location tab on its detail.
 *
 * The same rows as [Encounter] read the other way round: that one carries the Pokemon because a route
 * is asking what is on it, and this one carries the place because a Pokemon is asking where it is.
 * #24 is the ticket that noticed the two screens are one component pointed in two directions.
 *
 * [chance] and [conditions] mean exactly what they mean on [Encounter], including that a rate is a
 * share of its own table and that the conditions are filters rather than alternatives.
 */
data class VariantEncounter(
    val id: Long,
    val locationSlug: String,
    val locationName: String,
    val areaSlug: String,
    val areaName: String?,
    val method: String,
    val minLevel: Int,
    val maxLevel: Int,
    val chance: Int,
    val conditions: List<String>,
)
