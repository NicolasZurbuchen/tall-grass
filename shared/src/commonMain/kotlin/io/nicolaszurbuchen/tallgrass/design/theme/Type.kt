package io.nicolaszurbuchen.tallgrass.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Placeholder — falls back to the system default until a real typeface is
// wired in. A family usually bundles several weights, e.g.:
//   FontFamily(
//       Font(Res.font.primary_light, FontWeight.Light),
//       Font(Res.font.primary_regular, FontWeight.Normal),
//       Font(Res.font.primary_medium, FontWeight.Medium),
//       Font(Res.font.primary_bold, FontWeight.Bold),
//   )
val PrimaryFontFamily
    @Composable get() = FontFamily.Default

val MonoFontFamily
    @Composable get() = FontFamily.Monospace

/**
 * Typography roles mapped to Material3 slots. The mapping matters more than
 * the numbers — keep every slot's purpose comment when you fork this, even
 * if you change sizes/weights.
 */
val AppTypography
    @Composable get() =
        Typography(
            // Largest numeric/hero display (e.g. a score, a big stat)
            displayLarge =
                TextStyle(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 72.sp,
                    lineHeight = 80.sp,
                    letterSpacing = (-2).sp,
                ),
            // Secondary numeric display (e.g. a step counter, a progress indicator)
            displayMedium =
                TextStyle(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    letterSpacing = (-1).sp,
                ),
            // Small all-caps section labels
            displaySmall =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 2.sp,
                ),
            // Screen-level titles
            headlineLarge =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp,
                ),
            // Section headers within a screen
            headlineMedium =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 1.5.sp,
                ),
            // Primary content headline (e.g. a question, a card title)
            headlineSmall =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.3).sp,
                ),
            // Button labels
            titleLarge =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 1.sp,
                ),
            // Primary interactive content text (e.g. list item, option text)
            titleMedium =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                ),
            // Small labels for grouped stats/categories
            titleSmall =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 1.sp,
                ),
            // Primary body text
            bodyLarge =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.sp,
                ),
            // Supporting body text
            bodyMedium =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                ),
            // Captions, metadata
            bodySmall =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.3.sp,
                ),
            // Short mono labels (e.g. option letters, codes)
            labelLarge =
                TextStyle(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                ),
            // Live numeric label (e.g. a counter or timer)
            labelMedium =
                TextStyle(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                ),
            // Tags, chips
            labelSmall =
                TextStyle(
                    fontFamily = PrimaryFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 1.sp,
                ),
        )
