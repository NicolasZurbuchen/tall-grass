package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.tallgrass.design.component.AppErrorBanner
import io.nicolaszurbuchen.tallgrass.design.theme.ShimmerPulse
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveBattleData
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveDetailHeader
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveDetailSkeleton
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveDetailTabRow
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveEffect
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveLearners
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.component.MoveMechanics
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_battle_data
import tallgrass.shared.generated.resources.move_detail_effect
import tallgrass.shared.generated.resources.move_detail_mechanics

/**
 * One move, under a coloured hero, in two tabs.
 *
 * **Details** is what the move is, in the order a reader asks for it: the numbers that decide whether
 * to use it, the sentence that says what it does, and the detail behind the sentence. Two of its three
 * blocks are conditional — 93 moves have no effect paragraph and 92 no mechanics, and those sections
 * are absent rather than empty. See `DECISIONS.md § A move's absent numbers are absent, not zero`.
 *
 * **Learned by** is who can use it, which is the other question and a different shape: a grid of
 * Pokemon rather than a column of facts.
 *
 * The hero does not scroll away. It is short, it carries the only way back, and there is no sheet to
 * drag over it the way a Pokemon's detail has — the tabs sit under it and each one scrolls its own
 * content.
 *
 * No status-bar padding on the hero: it is a full-bleed colour and the arrow inside it carries the
 * inset instead, which is what makes the tint run to the top of the screen.
 */
@Composable
fun MoveDetailScreen(
    state: MoveDetailUiModel,
    onTabClick: (MoveDetailTabUiModel) -> Unit,
    onLearnerClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.error != null -> {
                AppErrorBanner(
                    text = state.error.title,
                    icon = state.error.icon,
                    onRetry = onRetryClick,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.move == null -> {
                ShimmerPulse { MoveDetailSkeleton() }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                    MoveDetailHeader(move = state.move, onBackClick = onBackClick)

                    MoveDetailTabRow(
                        selected = state.tab,
                        onTabClick = onTabClick,
                        modifier =
                            Modifier
                                .padding(horizontal = MaterialTheme.spacing.md)
                                .padding(top = MaterialTheme.spacing.sm),
                    )

                    when (state.tab) {
                        MoveDetailTabUiModel.DETAILS -> {
                            DetailsTab(move = state.move)
                        }

                        MoveDetailTabUiModel.LEARNERS -> {
                            MoveLearners(
                                learners = state.learners,
                                onLearnerClick = onLearnerClick,
                                modifier = Modifier.padding(MaterialTheme.spacing.md),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsTab(
    move: MoveContentUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.md),
    ) {
        SectionTitle(title = Res.string.move_detail_battle_data, isFirst = true)
        MoveBattleData(bars = move.bars, tint = move.type.color)

        move.effect?.let { effect ->
            SectionTitle(title = Res.string.move_detail_effect)
            MoveEffect(
                effect = effect,
                targetText = move.targetText,
                priorityText = move.priorityText,
            )
        }

        if (move.facts.isNotEmpty()) {
            SectionTitle(title = Res.string.move_detail_mechanics)
            MoveMechanics(facts = move.facts)
        }
    }
}

/**
 * The same heading the Pokemon detail's tabs use, so a section reads the same on both screens.
 *
 * [isFirst] drops the leading gap: the tab row above already provides it, and a second one reads as
 * the tab having failed to start.
 */
@Composable
private fun SectionTitle(
    title: StringResource,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.appColors.textPrimary,
        modifier =
            modifier.padding(
                top = if (isFirst) MaterialTheme.spacing.sm else MaterialTheme.spacing.lg,
                bottom = MaterialTheme.spacing.sm,
            ),
    )
}
