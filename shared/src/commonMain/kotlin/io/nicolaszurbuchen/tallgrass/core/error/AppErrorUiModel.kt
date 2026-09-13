package io.nicolaszurbuchen.tallgrass.core.error

import androidx.compose.ui.graphics.vector.ImageVector
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

data class AppErrorUiModel(
    val title: UiText,
    val subtitle: UiText,
    val icon: ImageVector,
)
