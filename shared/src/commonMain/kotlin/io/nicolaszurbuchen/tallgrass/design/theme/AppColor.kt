package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** App-level semantic color layer, sitting on top of Material's ColorScheme. */
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val borderSubtle: Color,
    val borderDefault: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val textInverse: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSubtle: Color,
    val onAccentSubtle: Color,
    val isDark: Boolean,
)

val DarkAppColors =
    AppColors(
        isDark = true,
        background = SlatePalette.slate950,
        surface = SlatePalette.slate800,
        surfaceRaised = SlatePalette.slate700,
        borderSubtle = SlatePalette.slate700,
        borderDefault = SlatePalette.slate600,
        textPrimary = SlatePalette.slate50,
        textSecondary = SlatePalette.slate200,
        textTertiary = SlatePalette.slate300,
        textDisabled = SlatePalette.slate500,
        textInverse = SlatePalette.slate900,
        accent = CobaltPalette.cobalt300,
        onAccent = SlatePalette.slate950,
        accentSubtle = CobaltPalette.cobalt900,
        onAccentSubtle = CobaltPalette.cobalt200,
    )

val LightAppColors =
    AppColors(
        isDark = false,
        background = SlatePalette.slate50,
        surface = Color(0xFFFFFFFF),
        surfaceRaised = SlatePalette.slate100,
        borderSubtle = SlatePalette.slate100,
        borderDefault = SlatePalette.slate200,
        textPrimary = SlatePalette.slate900,
        textSecondary = SlatePalette.slate500,
        textTertiary = SlatePalette.slate400,
        textDisabled = SlatePalette.slate300,
        textInverse = SlatePalette.slate50,
        accent = CobaltPalette.cobalt600,
        onAccent = Color(0xFFFFFFFF),
        accentSubtle = CobaltPalette.cobalt100,
        onAccentSubtle = CobaltPalette.cobalt800,
    )

internal val LocalTallGrassColors = staticCompositionLocalOf { DarkAppColors }

val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalTallGrassColors.current
