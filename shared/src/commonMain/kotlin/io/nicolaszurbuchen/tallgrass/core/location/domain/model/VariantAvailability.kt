package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * Where one Pokemon can be got, summarised above its availability grid.
 *
 * [versions] is every version in the app rather than one region's, because this side of the grid is
 * not scoped to a region: the question is which of my games has this, and the answer spans all of
 * them. [encounteredVersions] is the subset that comes up green.
 *
 * A grey cell stays tappable, which is how the two empty states get told apart at the moment somebody
 * asks. Scarlet answers "there is no data for this game yet" -- the gap #21 decided to ship openly --
 * and Black answers "transfer it in from another game", which is a fact about the Pokemon. Showing
 * the first wording for the second case would be actively wrong.
 */
data class VariantAvailability(
    val variantSlug: String,
    val captureMethods: List<CaptureMethod>,
    val versions: List<GameVersion>,
    val encounteredVersions: Set<String>,
)
