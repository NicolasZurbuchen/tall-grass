package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * The coarse summary at the top of a Pokemon's Location tab.
 *
 * These are **not** a collapse of upstream's 67 encounter methods, which is what #8 first assumed.
 * They come from unrelated places, and one of them is not an encounter method at all -- which is
 * exactly why #9 dropped the aggregate "methods across all games" list. No list of methods can say
 * "you cannot get this here".
 *
 * [TRANSFER_ONLY] is an **absence**: no rows in the newest generation that has any data. [TRADE] is
 * the `npc-trade` method, [GIFT_OR_EVENT] the `gift` one, and [WILD_CATCH] any row at all.
 *
 * **There is no Evolve pill, and #8 asks for one.** It is the one member that cannot come from
 * encounters -- it means being a non-base node in an evolution chain -- and this dataset has no
 * evolution chains in it yet. A member nothing can ever produce would read as coverage, so it is
 * absent until #6's data arrives rather than declared and never returned.
 *
 * Fossil is deliberately not here either. Omanyte, Kabuto, Aerodactyl and Tyrunt all surface as plain
 * gifts upstream, indistinguishable from Pikachu's sixteen, and hand-curating thirty species was
 * rejected for consistency with the Generation IX and Bulbapedia decisions. [GIFT_OR_EVENT] covers
 * them, and covers starters and event distributions the original five-pill set had no answer for.
 */
enum class CaptureMethod {
    WILD_CATCH,
    TRADE,
    GIFT_OR_EVENT,
    TRANSFER_ONLY,
}
