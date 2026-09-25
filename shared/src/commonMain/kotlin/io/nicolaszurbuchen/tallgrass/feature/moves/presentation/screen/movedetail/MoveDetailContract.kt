package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner

sealed interface MoveDetailIntent {
    data class TabSelected(
        val tab: MoveDetailState.Tab,
    ) : MoveDetailIntent

    data class LearnerClicked(
        val variantSlug: String,
    ) : MoveDetailIntent

    data object RetryClicked : MoveDetailIntent
}

/**
 * The one thing this screen publishes outward: a tap on a Pokemon in the Learned by tab.
 *
 * It carries what the Pokemon's hero opens with rather than only the slug, for the same reason a dex
 * card's tap does — the hero draws the artwork and the colour on its first frame, and this list has
 * both. See `HeroHandoff` in the Pokedex feature, which this cannot name: a feature may only import
 * from its own subtree, so `app/` is where these become a destination.
 *
 * [cardSlug] and [formSlug] are the same string for most Pokemon and not for any variant: only default
 * forms are dex cards, so Alolan Exeggutor is reached through Exeggutor. The card is what the detail's
 * carousel swipes along and the form is what it opens on.
 */
sealed interface MoveDetailLabel {
    data class NavigateToPokemon(
        val cardSlug: String,
        val formSlug: String,
        val name: String,
        val artworkUrl: String,
        val primaryTypeSlug: String,
        val secondaryTypeSlug: String?,
    ) : MoveDetailLabel
}

sealed interface MoveDetailAction {
    data object LoadMove : MoveDetailAction
}

sealed interface MoveDetailMessage {
    data object LoadStarted : MoveDetailMessage

    data class MoveLoaded(
        val move: MoveDetail,
        val learners: List<MoveLearner>,
    ) : MoveDetailMessage

    data class LoadFailed(
        val error: AppError,
    ) : MoveDetailMessage

    data class TabChanged(
        val tab: MoveDetailState.Tab,
    ) : MoveDetailMessage
}

/**
 * [move] is null until the read lands. Unlike the Pokemon detail there is nothing to draw before
 * then: a dex card hands its artwork, name and types forward so the hero renders on the first frame,
 * and a move card has no image to hand. See #11 on why this transition is a push.
 *
 * [learners] arrives with the move rather than when its tab is opened. The two reads are one round
 * trip to the same local database, and a tab that populates a beat after it is tapped reads as slower
 * than one that was always ready.
 */
data class MoveDetailState(
    val isLoading: Boolean = true,
    val move: MoveDetail? = null,
    val learners: List<MoveLearner> = emptyList(),
    val tab: Tab = Tab.DETAILS,
    val error: AppError? = null,
) {
    /**
     * Nested rather than a type of its own, on the precedent of `DetailState.Tab`: a Contract holds
     * the Store's vocabulary, and which tab is open is a fact about this Store and nothing else. The
     * rendering half is `MoveDetailTabUiModel`.
     */
    enum class Tab {
        DETAILS,
        LEARNERS,
    }
}
