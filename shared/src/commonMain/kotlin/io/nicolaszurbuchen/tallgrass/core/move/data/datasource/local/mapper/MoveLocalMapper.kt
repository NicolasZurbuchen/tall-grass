package io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMove
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoveLearners
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoveStatChanges
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoves
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.BattleStat
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveAilment
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveCategory
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveMeta
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveStatChange
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveTarget
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * Null when the row names a type or a damage class this build does not know, which means the bundled
 * dataset and this build disagree. A row dropped from the list is better than a card with no colour.
 */
fun SelectMoves.toDomain(): Move? {
    val type = PokemonType.fromSlug(typeSlug) ?: return null
    val damageClass = DamageClass.fromSlug(this.damageClass) ?: return null

    return Move(
        slug = slug,
        name = name,
        type = type,
        damageClass = damageClass,
        power = power?.toInt(),
    )
}

/** Null on the same grounds as the list mapper's, plus a target this build does not know. */
fun SelectMove.toDomain(statChanges: List<SelectMoveStatChanges>): MoveDetail? {
    val type = PokemonType.fromSlug(typeSlug) ?: return null
    val damageClass = DamageClass.fromSlug(this.damageClass) ?: return null
    val target = MoveTarget.fromSlug(this.target) ?: return null

    return MoveDetail(
        slug = slug,
        name = name,
        generation = generation.toInt(),
        type = type,
        damageClass = damageClass,
        power = power?.toInt(),
        accuracy = accuracy?.toInt(),
        pp = pp?.toInt(),
        priority = priority.toInt(),
        target = target,
        shortEffect = shortEffect,
        effect = effect,
        meta = toMetaDomain(),
        // Sorted here rather than in SQL: the table has no ordering column, and BattleStat's
        // declaration order is the one every other screen draws stats in.
        statChanges =
            statChanges
                .mapNotNull { row ->
                    BattleStat.fromSlug(row.statSlug)?.let { MoveStatChange(it, row.change.toInt()) }
                }.sortedBy { it.stat.ordinal },
    )
}

/**
 * The twelve meta columns are null together or populated together, so the category standing in for
 * all of them is a read of the row rather than a guess: it is the one that is never null when
 * upstream has filled the move in.
 */
private fun SelectMove.toMetaDomain(): MoveMeta? {
    val category = this.category?.let(MoveCategory::fromSlug) ?: return null

    return MoveMeta(
        category = category,
        ailment = ailment?.let(MoveAilment::fromSlug),
        ailmentChance = ailmentChance?.toInt(),
        minHits = minHits?.toInt(),
        maxHits = maxHits?.toInt(),
        minTurns = minTurns?.toInt(),
        maxTurns = maxTurns?.toInt(),
        drain = drain?.toInt(),
        healing = healing?.toInt(),
        critRate = critRate?.toInt(),
        flinchChance = flinchChance?.toInt(),
        statChance = statChance?.toInt(),
    )
}

/**
 * Null when the row names a method or a primary type this build does not know, which means the
 * bundled dataset and this build disagree. A dropped learner is better than a card that cannot say
 * how, or one with no colour. A missing second type is the ordinary case, not a failure.
 */
fun SelectMoveLearners.toDomain(): MoveLearner? {
    val method = LearnMethod.fromSlug(this.method) ?: return null
    val primary = primaryType?.let(PokemonType::fromSlug) ?: return null

    return MoveLearner(
        variantSlug = slug,
        dexNumber = speciesDexNumber.toInt(),
        name = name,
        formLabel = formLabel,
        artworkUrl = artworkUrl,
        primaryType = primary,
        secondaryType = secondaryType?.let(PokemonType::fromSlug),
        method = method,
        level = level?.toInt(),
    )
}
