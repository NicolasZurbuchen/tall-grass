package io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoveLearners
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoveStatChanges
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMoves
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.SelectMovesForVariant
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
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.Move as MoveRow

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
fun MoveRow.toDomain(statChanges: List<SelectMoveStatChanges>): MoveDetail? {
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
private fun MoveRow.toMetaDomain(): MoveMeta? {
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

    // A species with no dex card is a hole in the grid, which another test already forbids. Dropping
    // the row is still better than a card that cannot be opened.
    val card = cardSlug ?: return null

    return MoveLearner(
        variantSlug = slug,
        cardSlug = card,
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

/**
 * Null on the same grounds as the other three: a type, damage class or method this build does not
 * know means the bundled dataset and this build disagree.
 */
fun SelectMovesForVariant.toDomain(): VariantMove? {
    val type = PokemonType.fromSlug(typeSlug) ?: return null
    val damageClass = DamageClass.fromSlug(this.damageClass) ?: return null
    val method = LearnMethod.fromSlug(this.method) ?: return null

    return VariantMove(
        slug = slug,
        name = name,
        type = type,
        damageClass = damageClass,
        power = power?.toInt(),
        method = method,
        level = level?.toInt(),
    )
}
