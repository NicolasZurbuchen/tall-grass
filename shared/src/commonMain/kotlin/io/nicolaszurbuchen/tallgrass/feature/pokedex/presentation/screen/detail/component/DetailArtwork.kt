package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.tallgrass.infra.navigation.SharedElementKey
import io.nicolaszurbuchen.tallgrass.infra.navigation.sharedElementOrNone

/**
 * The hero's artwork, and the receiving half of the transition from a dex card.
 *
 * The URL arrives with the destination, so Coil answers from its memory cache — the card that was
 * tapped is still showing the same image — and the first frame is already the picture. Resolving it
 * here instead is what makes a hero flash empty on arrival.
 *
 * [artworkKey] matches a card only while the form on screen is the one that was tapped; after a
 * switch it deliberately matches nothing. The modifier is applied either way, because dropping it
 * would move a `remember` in and out of the composition every time the switcher is used.
 *
 * Its size is the caller's, because where the artwork sits relative to the sheet is a decision about
 * the screen rather than about the artwork.
 */
@Composable
fun DetailArtwork(
    artworkUrl: String,
    artworkKey: SharedElementKey,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = artworkUrl,
        // The name is read out beside it, so describing the artwork too would say every Pokemon
        // twice to a screen reader.
        contentDescription = null,
        modifier = modifier.sharedElementOrNone(artworkKey),
    )
}
