package io.nicolaszurbuchen.tallgrass.infra.platform

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
