package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightMaterialColorScheme =
    lightColorScheme(
        primary = CobaltPalette.cobalt600,
        onPrimary = SlatePalette.slate50,
        primaryContainer = CobaltPalette.cobalt100,
        onPrimaryContainer = CobaltPalette.cobalt900,
        background = SlatePalette.slate50,
        onBackground = SlatePalette.slate900,
        surface = SlatePalette.slate50,
        onSurface = SlatePalette.slate900,
        surfaceVariant = SlatePalette.slate100,
        onSurfaceVariant = SlatePalette.slate500,
        outline = SlatePalette.slate200,
        outlineVariant = SlatePalette.slate100,
    )

private val DarkMaterialColorScheme =
    darkColorScheme(
        primary = CobaltPalette.cobalt300,
        onPrimary = SlatePalette.slate950,
        primaryContainer = CobaltPalette.cobalt900,
        onPrimaryContainer = CobaltPalette.cobalt200,
        background = SlatePalette.slate950,
        onBackground = SlatePalette.slate50,
        surface = SlatePalette.slate800,
        onSurface = SlatePalette.slate50,
        surfaceVariant = SlatePalette.slate700,
        onSurfaceVariant = SlatePalette.slate300,
        outline = SlatePalette.slate600,
        outlineVariant = SlatePalette.slate700,
    )

/** Root theme composable. Wire any additional domain color layer here alongside LocalAppColors. */
@Composable
fun TallGrassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val materialColors = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme

    MaterialTheme(
        colorScheme = materialColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = {
            CompositionLocalProvider(
                LocalSpacing provides Spacing(),
                LocalTallGrassColors provides appColors,
            ) {
                content()
            }
        },
    )
}
