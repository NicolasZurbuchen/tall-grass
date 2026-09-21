package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.core.error.AppErrorUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveLearnerUiModel

@Immutable
data class MoveDetailUiModel(
    val isLoading: Boolean,
    val move: MoveContentUiModel?,
    val learners: List<MoveLearnerUiModel>,
    val tab: MoveDetailTabUiModel,
    val error: AppErrorUiModel?,
)
