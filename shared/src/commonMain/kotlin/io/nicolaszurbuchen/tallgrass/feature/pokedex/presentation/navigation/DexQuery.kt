package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Which list the dex was showing when a card was tapped.
 *
 * Carried on the destination so the detail's carousel traverses the same list rather than the whole
 * dex: pick Fire, open Charmander, and swiping stays in Fire. Today there is one list and one value.
 *
 * The *query* rather than the list it returns. A `NavKey` holding a thousand slugs is twenty
 * kilobytes written into saved state on every navigation and read back on process death, and it
 * freezes a result set that the dataset can move under. The query is small, it is the identity of
 * the result set, and the detail re-runs it.
 *
 * See `DECISIONS.md § The carousel traverses the query, not a copy of its results`.
 */
@Serializable
sealed interface DexQuery {
    /** Every card in the dex, in National Dex order. */
    @Serializable
    data object All : DexQuery
}
