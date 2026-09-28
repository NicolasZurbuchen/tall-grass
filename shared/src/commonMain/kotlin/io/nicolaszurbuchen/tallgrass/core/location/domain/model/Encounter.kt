package io.nicolaszurbuchen.tallgrass.core.location.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One thing that can be met in one place in one game, for the location's own screen.
 *
 * [chance] is a share of its **table**, and a table is ([areaSlug], [method]) -- never of the place,
 * and never of the method across the place. A location can draw one method from several areas and
 * one of them draws it from 22, so adding those together is how a bar reaches 2,200%.
 *
 * [chance] is 0 where the method has no meaningful rate at all: raids, gifts, trades, SOS calls. The
 * row prints nothing there rather than a fabricated percentage.
 *
 * [conditions] are **AND-filters on this row**, not a list of alternatives. The row is possible only
 * when every one of them is satisfied, which is what lets a dozen rows that look like 360% of a table
 * resolve to exactly 100% once a state is chosen. See the correction on #8.
 *
 * [cardSlug] is the dex card this is reached through, which is not always the Pokemon itself: only
 * default forms are cards, so an Alolan Sandshrew opens through Sandshrew.
 */
data class Encounter(
    val id: Long,
    val variantSlug: String,
    val cardSlug: String,
    val dexNumber: Int,
    val name: String,
    val artworkUrl: String,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val areaSlug: String,
    val areaName: String?,
    val method: String,
    val minLevel: Int,
    val maxLevel: Int,
    val chance: Int,
    val conditions: List<String>,
)
