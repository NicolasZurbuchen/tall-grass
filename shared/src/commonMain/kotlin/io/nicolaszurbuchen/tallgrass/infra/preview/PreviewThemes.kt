package io.nicolaszurbuchen.tallgrass.infra.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders the annotated preview twice, light and dark.
 *
 * Do not pass a `darkTheme` argument alongside it — the ui mode set here is what the theme reads, so
 * the dark rendering is the real one. See DECISIONS.md § A preview brings its own ground.
 */
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = PreviewUiMode.NIGHT_YES)
annotation class PreviewThemes
