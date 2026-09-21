package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.core.move.presentation.component.DamageClassIcon
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantMoveUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_no_moves

/**
 * Every move this form learns, level-up first and by level within it.
 *
 * **Rows rather than the grid the moves list uses.** That screen is a browse — you are looking for a
 * move you cannot name, so the cards are big and the colour does the sorting. This is a reference:
 * the reader already has a Pokemon and wants to run down what it knows, which is a column of about
 * sixty and reads fastest as lines.
 *
 * The type still colours each row, because that is what makes the list scannable at all, and the
 * damage-class glyph still says how the move deals its damage. A row is the move card compressed to
 * one line rather than a different design.
 *
 * Abilities belong on this tab too and are not here yet: they need `core/ability/`, which arrives
 * with the abilities feature. See #11 and #27.
 */
@Composable
fun MovesTab(
    moves: List<VariantMoveUiModel>,
    onMoveClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (moves.isEmpty()) {
        // Every Mega and Gigantamax, which learn what their base form learns and which upstream does
        // not repeat rows for. Said out loud, because an empty tab reads as a read that has not
        // finished rather than as an answer.
        Text(
            text = stringResource(Res.string.pokedex_detail_no_moves),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.appColors.textSecondary,
            modifier = modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.md),
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = modifier.fillMaxWidth(),
    ) {
        moves.forEach { move ->
            MoveRow(move = move, onClick = { onMoveClick(move.slug) })
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
