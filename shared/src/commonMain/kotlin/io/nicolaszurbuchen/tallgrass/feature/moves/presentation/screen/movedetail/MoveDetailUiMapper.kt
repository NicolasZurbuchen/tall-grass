package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveDetailTabUiModel

fun MoveDetailState.toUiModel(): MoveDetailUiModel =
    MoveDetailUiModel(
        isLoading = isLoading,
        move = move?.toUiModel(),
        learners = learners.map { it.toUiModel() },
        tab =
            when (tab) {
                MoveDetailState.Tab.DETAILS -> MoveDetailTabUiModel.DETAILS
                MoveDetailState.Tab.LEARNERS -> MoveDetailTabUiModel.LEARNERS
            },
        error = error?.toUiModel(),
    )
