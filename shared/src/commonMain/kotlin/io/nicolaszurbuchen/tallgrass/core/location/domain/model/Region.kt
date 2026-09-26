package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One card in the region list.
 *
 * [nativeName] is null for Orre alone, which upstream has no Japanese name for. Every other region
 * has one and it is free in upstream's names table, so the card carries it.
 *
 * [boxArt] is the pair of artwork URLs the card is built around. A single defining mascot was
 * rejected in #24 as an arbitrary editorial pick -- there is no argument for Ho-Oh over Lugia -- and
 * a pair removes the choice. **It is not always two**: Hisui has one, because Legends: Arceus shipped
 * without a pair on the cover.
 */
data class Region(
    val slug: String,
    val name: String,
    val nativeName: String?,
    val generation: Int,
    val locationCount: Int,
    val boxArt: List<String>,
)
