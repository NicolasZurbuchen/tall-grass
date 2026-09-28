package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * One card in the region list.
 *
 * [nativeName] is free in upstream's names table. It is not drawn on the card any more -- two columns
 * of cards have no room for a third line -- but the About tab shows it.
 *
 * [boxArt] is the pair of artwork URLs the card is built around. A single defining mascot was
 * rejected in #24 as an arbitrary editorial pick -- there is no argument for Ho-Oh over Lugia -- and
 * a pair removes the choice. **It is not always two**: Hisui has one, because Legends: Arceus shipped
 * without a pair on the cover.
 */
data class Region(
    val slug: String,
    val name: String,
    val nativeName: String,
    val generation: Int,
    val locationCount: Int,
    val boxArt: List<String>,
)
