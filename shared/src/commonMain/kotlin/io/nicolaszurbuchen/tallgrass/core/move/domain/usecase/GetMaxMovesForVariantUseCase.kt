package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MaxMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMaxMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * What one Gigantamax form actually has in front of it, which is not the list of moves it learnt.
 *
 * **A Dynamaxed Pokemon does not use its own moves.** Each damaging move becomes the Max Move of its
 * type, every status move becomes Max Guard, and on a Gigantamax form the moves matching its
 * signature type become its own G-Max Move instead of the ordinary Max Move. Sixty-odd moves come
 * out as ten or so.
 *
 * The collapse is the reason [VariantMaxMove.sources] exists. A Charizard that knows Flamethrower,
 * Fire Blast, Heat Wave and Ember has one G-Max Wildfire, and without the moves that feed it the
 * list reads as a Pokemon that has forgotten most of what it knew.
 *
 * **Power is the best of the sources rather than a figure of the move's own.** Max Flare is 130 from
 * Flamethrower and 90 from Ember, so a Pokemon with both reaches 130 — which is the number worth
 * printing, because it is what that Pokemon can do. The three G-Max Moves with a fixed 160 ignore
 * the sources entirely, and say so in the dataset.
 */
class GetMaxMovesForVariantUseCase(
    private val repository: MoveRepository,
) {
    /**
     * [gmaxMoveSlug] is the form's own G-Max Move, from `PokemonVariant.gmaxMove`. Null gives the
     * ordinary Dynamax conversion, which is every Pokemon's and is what the Gigantamax forms would
     * have without their signature.
     */
    suspend operator fun invoke(
        variantSlug: String,
        gmaxMoveSlug: String?,
    ): List<VariantMaxMove> {
        val moves = repository.movesFor(variantSlug)
        if (moves.isEmpty()) return emptyList()

        val catalogue = repository.maxMoves().associateBy { it.slug }
        // Max Guard is excluded rather than filtered by type, and the difference matters: it is a
        // Normal-type move and so is Max Strike, so leaving it in would give one of them the other's
        // place in the map depending on which was read last.
        val byType =
            catalogue.values
                .filter { it.slug.startsWith(MAX_MOVE_PREFIX) && it.slug != MAX_GUARD }
                .associateBy { it.type }
        val guard = catalogue[MAX_GUARD]
        val signature = gmaxMoveSlug?.let { catalogue[it] }

        return moves.groupBy { move -> move.becomes(byType, guard, signature) }
            .mapNotNull { (target, sources) -> target?.toVariantMaxMove(sources, signature) }
            // Strongest first, which puts Max Guard last: it is the one row with no power, and it is
            // also the one every Pokemon has, so it is the least worth reading.
            .sortedWith(compareByDescending<VariantMaxMove> { it.power ?: 0 }.thenBy { it.name })
    }
}

/**
 * Which Max Move this one turns into, or null when nothing in the catalogue covers it.
 *
 * Null is reachable and is not defensiveness: a move of a type this build has no Max Move for would
 * mean the dataset and the catalogue disagree, and dropping the row is better than inventing one.
 */
private fun VariantMove.becomes(
    byType: Map<PokemonType, MaxMove>,
    guard: MaxMove?,
    signature: MaxMove?,
): MaxMove? =
    when {
        damageClass == DamageClass.STATUS -> guard

        // Ahead of the type lookup, because that is the whole of what the Gigantamax factor changes.
        signature != null && type == signature.type -> signature

        else -> byType[type]
    }

private fun MaxMove.toVariantMaxMove(
    sources: List<VariantMove>,
    signature: MaxMove?,
): VariantMaxMove {
    // The move that reaches furthest with this one, which decides both the power and the category:
    // a Max Move takes its category from whatever it replaced, and showing the strongest source's is
    // more use than two rows differing in a field nothing else on the row reflects.
    val best = sources.maxWithOrNull(compareBy({ it.maxPower ?: 0 }, { it.power ?: 0 })) ?: sources.first()

    return VariantMaxMove(
        slug = slug,
        name = name,
        type = type,
        damageClass = best.damageClass,
        // The move's own power wins where it has one, which is the three that are 160 whatever they
        // replaced.
        power = power ?: best.maxPower,
        isSignature = slug == signature?.slug,
        sources = sources.sortedWith(compareByDescending<VariantMove> { it.maxPower ?: 0 }.thenBy { it.name }),
    )
}

/** The 18 ordinary Max Moves share it; the 33 G-Max Moves do not, which is how the two are told apart. */
private const val MAX_MOVE_PREFIX = "max-"

/** Every status move becomes this one, so it is named rather than found by type. */
private const val MAX_GUARD = "max-guard"
