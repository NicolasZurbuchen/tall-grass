package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region

sealed interface RegionsIntent {
    data class RegionClicked(
        val slug: String,
    ) : RegionsIntent

    data object RetryClicked : RegionsIntent
}

sealed interface RegionsLabel {
    data class NavigateToDetail(
        val slug: String,
    ) : RegionsLabel
}

sealed interface RegionsAction {
    data object LoadRegions : RegionsAction
}

sealed interface RegionsMessage {
    data object LoadStarted : RegionsMessage

    data class RegionsLoaded(
        val regions: List<Region>,
    ) : RegionsMessage

    data class LoadFailed(
        val error: AppError,
    ) : RegionsMessage
}

data class RegionsState(
    val isLoading: Boolean = true,
    val regions: List<Region> = emptyList(),
    val error: AppError? = null,
)
