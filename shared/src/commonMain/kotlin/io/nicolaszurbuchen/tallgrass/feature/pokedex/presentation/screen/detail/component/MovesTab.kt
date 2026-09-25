package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.move.presentation.component.DamageClassIcon
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantAbilityUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantMoveUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_abilities
import tallgrass.shared.generated.resources.pokedex_detail_moves_heading
import tallgrass.shared.generated.resources.pokedex_detail_no_moves

/**
 * What this form can do: the abilities it has, then every move it learns.
 *
 * **Abilities are here rather than on About**, which is #27's call over the layout #11 implied:
 * About is the summary and this tab is what the Pokemon can *do*, and listing them in both read as
 * redundant. They go above the moves because there are two or three of them against about sixty, and
 * because an ability is always on where a move has to be chosen.
 *
 * **Moves are rows rather than the grid the moves list uses.** That screen is a browse — you are
 * looking for a move you cannot name, so the cards are big and the colour does the sorting. This is a
 * reference: the reader already has a Pokemon and wants to run down what it knows, which reads
 * fastest as lines.
 *
 * The two kinds of row are deliberately unalike. A move row is type-coloured because that is what
 * makes sixty of them scannable; an ability row is a surface with a lettered tile, matching the
 * abilities list. Headings say which is which, and would be doing that work even if the rows looked
 * the same.
 */
@Composable
fun MovesTab(
    abilities: List<VariantAbilityUiModel>,
    moves: List<VariantMoveUiModel>,
    onAbilityClick: (String) -> Unit,
    onMoveClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Absent rather than empty-stated. Every Pokemon has at least one ability, so nothing here
        // means the read has not landed — and a heading over a "none" that cannot happen would be
        // answering a question nobody asked.
        if (abilities.isNotEmpty()) {
            SectionHeading(title = Res.string.pokedex_detail_abilities, isFirst = true)

            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                abilities.forEach { ability ->
                    AbilityRow(ability = ability, onClick = { onAbilityClick(ability.slug) })
                }
            }
        }

        SectionHeading(title = Res.string.pokedex_detail_moves_heading, isFirst = abilities.isEmpty())

        if (moves.isEmpty()) {
            // Every Mega and Gigantamax, which learn what their base form learns and which upstream
            // does not repeat rows for. Said out loud, because an empty list reads as a read that has
            // not finished rather than as an answer.
            Text(
                text = stringResource(Res.string.pokedex_detail_no_moves),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
            return@Column
        }

        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            moves.forEach { move ->
                MoveRow(move = move, onClick = { onMoveClick(move.slug) })
            }
        }
    }
}

/**
 * [isFirst] drops the space above, because the tab already has its own padding and a heading at the
 * top would sit twice as far down as one in the middle.
 */
@Composable
private fun SectionHeading(
    title: StringResource,
    isFirst: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.appColors.textPrimary,
        modifier =
            modifier.padding(
                top = if (isFirst) MaterialTheme.spacing.xs else MaterialTheme.spacing.lg,
                bottom = MaterialTheme.spacing.sm,
            ),
    )
}

@Composable
private fun AbilityRow(
    ability: VariantAbilityUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surfaceRaised),
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(TILE_SIZE)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.appColors.accentSubtle),
            ) {
                Text(
                    text = ability.initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.appColors.onAccentSubtle,
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = ability.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.appColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    ability.hiddenText?.let { hidden ->
                        Text(
                            text = hidden.asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.appColors.textTertiary,
                            maxLines = 1,
                        )
                    }
                }

                // Two lines and an ellipsis, matching the abilities list: the detail is one tap away
                // and this row is here to say which abilities the Pokemon has, not to explain them.
                Text(
                    text = ability.shortEffect,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                    maxLines = EFFECT_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun MoveRow(
    move: VariantMoveUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = move.type.color, contentColor = Color.White),
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
        ) {
            DamageClassIcon(
                damageClass = move.damageClass,
                color = Color.White.copy(alpha = GLYPH_ALPHA),
                modifier = Modifier.size(GLYPH_WIDTH),
            )

            Text(
                text = move.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = move.howText.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = HOW_ALPHA),
                maxLines = 1,
            )

            // A fixed slot so the figures line up down the column, which is the whole point of
            // putting them last. An em dash sits in it for the status moves.
            Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.size(width = POWER_WIDTH, height = ROW_TEXT)) {
                Text(text = move.powerText, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// Small enough to read as a marker beside the name rather than as the row's subject. The vectors are
// 3:2 and DamageClassIcon applies the aspect, so this is a width.
private val GLYPH_WIDTH = 22.dp

// Fainter than the name, which is what the row is for.
private const val GLYPH_ALPHA = 0.85f

// Supporting text on a saturated ground, where full white reads as loud as the name.
private const val HOW_ALPHA = 0.75f

// Three digits at bodyLarge, which is the widest a power gets.
private val POWER_WIDTH = 34.dp
private val ROW_TEXT = 22.dp

// Smaller than the abilities list's 44, because the row is one of three rather than one of 314 and
// the tile is a marker here rather than the thing carrying the scroll.
private val TILE_SIZE = 40.dp

private const val EFFECT_LINES = 2
