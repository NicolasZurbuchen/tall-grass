package io.nicolaszurbuchen.tallgrass.core.ability.domain.model

/**
 * One row in the abilities list.
 *
 * Deliberately thin, like [io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move]: the list holds
 * all 314 at once and everything a card draws is here. The long [AbilityDetail.effect] is not, and
 * that is the point — it averages 205 characters and the list draws none of it.
 *
 * [generation] is the only axis these rows carry. Upstream has no category for abilities and three
 * attempts at inventing one were rejected, so until #65 answers that, this is what a card has to
 * distinguish itself by.
 */
data class Ability(
    val slug: String,
    val name: String,
    val generation: Int,
    val shortEffect: String,
)
