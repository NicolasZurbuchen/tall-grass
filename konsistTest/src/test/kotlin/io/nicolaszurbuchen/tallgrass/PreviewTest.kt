package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * A `*ScreenPreview` file is the only place a screen is ever seen without a device, so it is worth
 * having a shape rather than a habit.
 *
 * **Two top-level declarations, and no more.** A provider class holding every fixture, and one
 * preview function. The pull is always to add a third — a helper that builds a UiModel, then a
 * second helper beside it, then a `private val` of sample data — and each one is individually
 * reasonable while the file stops being readable as "here are the states, here is the screen". The
 * fixtures belong *inside* the provider, where they are visibly in service of the sequence they
 * feed.
 *
 * **One preview function, not one per theme.** Light and dark are two renderings of one preview,
 * which is what a multipreview annotation is for: `PreviewThemes` carries both, so the body is
 * written once and a third rendering later is a change to the annotation rather than to every
 * preview in the app.
 */
class PreviewTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")

        /**
         * Deliberately narrower than "Preview": `TallGrassPreview` and `PreviewThemes` are the
         * vocabulary these rules are written in, and holding the vocabulary to the shape rules of
         * the thing that uses it would be circular.
         */
        private const val PREVIEW_SUFFIX = "ScreenPreview"

        /** A screen's own subfolders. A file in one of these is not a screen. */
        private val SCREEN_SUBPACKAGES = listOf("component", "uimodel", "mapper")
    }

    // region every screen has one

    @Test
    fun `every screen folder must contain a Preview file`() {
        // A screen with no preview is a screen nobody has looked at in the states it can reach — and
        // the states that go wrong are never the one the device happens to open on.
        val screenPackages =
            scope.files
                .withPackage("..presentation.screen..")
                .filter { file -> SCREEN_SUBPACKAGES.none { sub -> file.hasPackage("..$sub") } }
                .groupBy { it.packagee?.name }

        screenPackages.forEach { (packageName, files) ->
            assert(files.any { it.name.endsWith(PREVIEW_SUFFIX) }) { "Missing Preview in $packageName" }
        }
    }

    // endregion

    // region shape

    @Test
    fun `Preview files must declare at most two top-level declarations`() {
        // The provider and the preview. Everything else is a fixture, and a fixture belongs inside
        // the provider it feeds.
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file ->
                val topLevel =
                    file.classes(includeNested = false) +
                        file.interfaces(includeNested = false) +
                        file.objects(includeNested = false) +
                        file.functions(includeNested = false)

                topLevel.size <= 2
            }
    }

    @Test
    fun `Preview files must declare exactly one top-level function`() {
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file -> file.functions(includeNested = false).size == 1 }
    }

    @Test
    fun `Preview files must not declare top-level helper properties`() {
        // Sample data hoisted to the top of the file reads as something the screen depends on rather
        // than as one of the states being previewed.
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file -> file.properties(includeNested = false).isEmpty() }
    }

    @Test
    fun `the Preview function must be private`() {
        // Nothing outside the file may call it, and a public one ends up in the shared module's API
        // surface for no reason.
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file ->
                file.functions(includeNested = false).all { !it.hasPublicOrDefaultModifier }
            }
    }

    // endregion

    // region the provider

    @Test
    fun `Preview files with a provider must name it after the file`() {
        // FooScreenPreview holds FooScreenStateProvider. A provider named for its content rather than
        // for its screen is one that gets copied into the next preview by accident.
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Preview")

                file.classes(includeNested = false).all { it.name == "${prefix}StateProvider" }
            }
    }

    @Test
    fun `a Preview provider must be private`() {
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file ->
                file.classes(includeNested = false).all { !it.hasPublicOrDefaultModifier }
            }
    }

    @Test
    fun `a Preview provider must implement PreviewParameterProvider`() {
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file ->
                file.classes(includeNested = false).all { clazz ->
                    clazz.parents().any { it.name.substringBefore("<") == "PreviewParameterProvider" }
                }
            }
    }

    // endregion

    // region what it renders

    @Test
    fun `Preview functions must use the PreviewThemes multipreview rather than Preview directly`() {
        // Two @Preview-annotated functions in one file is the old shape: the same body written twice
        // so it can be seen on two grounds. PreviewThemes is that pair, declared once.
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .flatMap { it.functions(includeNested = false) }
            .filterNot { it.hasAnnotationWithName("PreviewThemes") }
            .assertEmpty()
    }

    @Test
    fun `Preview functions must render inside the preview harness`() {
        // Two things at once, which is why it is one wrapper rather than two rules. The theme,
        // because a preview drawn outside it is a preview of Material's defaults — every colour on
        // these screens comes from AppColors, which only exists inside it. And the ground, because
        // Compose's preview pane paints its own white whatever the theme says, so a screen that does
        // not fill its background renders dark-theme text on a white sheet and looks fine.
        scope.files
            .withNameEndingWith(PREVIEW_SUFFIX)
            .assertTrue { file -> file.hasImport { it.name.endsWith(".TallGrassPreview") } }
    }

    // endregion

    // region where the vocabulary lives

    /**
     * **The vocabulary is split, and the split is the placement rule applied literally.**
     *
     * `PreviewThemes` and `PreviewUiMode` know nothing about this app. The annotation sets a system
     * ui-mode flag and the object names two Android constants commonMain cannot import; both would
     * work unchanged in any Compose app, which is the definition of plumbing. They sit beside
     * `infra/text/UiText` and `infra/platform/BackHandler`, which are Compose code in `infra/` for
     * exactly the same reason.
     *
     * `TallGrassPreview` is the opposite: it imports the theme and the palette, so it *is* the design
     * system and could not live in `infra/` without inverting the layering.
     */
    @Test
    fun `the preview vocabulary must be declared once, in one place each`() {
        scope.files
            .filter { it.name in setOf("PreviewThemes", "PreviewUiMode") }
            .assertTrue { it.hasPackage("..infra.preview") }

        scope.files
            .filter { it.name == "TallGrassPreview" }
            .assertTrue { it.hasPackage("..design.preview") }
    }
    // endregion
}
