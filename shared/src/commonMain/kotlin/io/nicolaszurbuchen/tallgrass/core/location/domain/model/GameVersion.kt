package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One playable version, which is one cell of the availability grid.
 *
 * Every version is its own cell and no two share one: FireRed and LeafGreen do not share an encounter
 * table, so they cannot share a cell, and nor can Red and Blue. See #9.
 *
 * [code] is unique only within [console], which is what the grouping is for rather than decoration --
 * `Y` is Yellow on the GB row and Pokemon Y on the 3DS row, and `B` is Blue on one and Black on
 * another. Drawing the codes without their rows makes two different games look like the same one.
 */
data class GameVersion(
    val slug: String,
    val name: String,
    val code: String,
    val console: Console,
    val generation: Int,
)
